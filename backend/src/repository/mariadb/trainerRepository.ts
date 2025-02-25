import Trainee from "../../domain/entities/heroAggregate/Trainee";
import mariadb from "mariadb";
import Trainer from "../../domain/entities/heroAggregate/Trainer";
import ITrainerRepository from "../../domain/entities/heroAggregate/ITrainerRepository";

export default class TrainerRepository implements ITrainerRepository {
  private constructor(
    private readonly pool: mariadb.Pool,
    private readonly database: string
  ) {}

  public static async createWithPool(pool: mariadb.Pool, database: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const connection = await pool.getConnection();
    try {
      // TODO: For the future we should also handle schema updates, for now: just ensure the tables are there
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`trainer` ( `userId` int not null PRIMARY KEY REFERENCES `'+database+'`.`user` (`id`), `maxTrainees` int not null, `lastTraining` int not null );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`trainee` ( `id` int not null PRIMARY KEY, `disposition` varchar(2048) not null, `xp` int not null, `lastTraining` int not null );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`training` ( `trainerId` int not null REFERENCES `'+database+'`.`trainer` (`userId`), `traineeId` int not null UNIQUE REFERENCES `'+database+'`.`trainee` ( `id` ) );');
    }
    finally {
      await connection.release();
    }

    return new TrainerRepository(pool, database);
  }

  public static create(host: string, port: number, database: string, user: string, password: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const pool = mariadb.createPool({
      host,
      user,
      password,
      database,
      port,
      connectionLimit: 50
    });
    return TrainerRepository.createWithPool(pool, database);
  }

  async getOrCreateTrainer(userId: number): Promise<Trainer> {
    const connection = await this.pool.getConnection();
    try {
      const trainerData = await connection.query<{maxTrainees:number,lastTraining:number}[]>(
        'SELECT `maxTrainees`, `lastTraining` FROM `'+this.database+'`.`trainer` WHERE `userId` = ?;', [userId]
      );
      if (trainerData.length > 1) {
        throw new Error(`Database corrupted: multiple trainers for one user (${userId})`);
      }
      if (trainerData.length < 1) {
        const newTrainerData = await connection.query(
          'INSERT INTO `'+this.database+'`.`trainer` (`userId`,`maxTrainees`,`lastTraining`) VALUES (?, 1, 0);', [userId]
        );
        trainerData.push({ maxTrainees: 1, lastTraining: 0 });
      }

      const traineeData = await connection.query<{id:number,xp:number,disposition:string,lastTraining:number}[]>(
        'SELECT `f`.`id`, `f`.`xp`, `f`.`disposition`, `f`.`lastTraining` '+
        'FROM `'+this.database+'`.`training` AS `t` '+
        'JOIN `'+this.database+'`.`trainee` AS `f` ON `f`.`id` = `t`.`traineeId` WHERE `t`.`userId` = ?;',
        [userId]
      );
      const trainer = new Trainer(userId, trainerData[0].maxTrainees, trainerData[0].lastTraining, traineeData.map((d) => {
        const dispoData: {[key:string]:number} = JSON.parse(d.disposition);
        const dispo = new Map<string, number>(Object.entries(dispoData));
        return new Trainee(d.id, d.xp, dispo, d.lastTraining);
      }));
      return trainer;
    }
    finally {
      await connection.release();
    }
  }

  async save(trainer: Trainer) {
    const connection = await this.pool.getConnection();
    try {
      await connection.beginTransaction();
      await connection.execute(
        'INSERT INTO `'+this.database+'`.`trainer` ( `userId`, `maxTrainees`, `lastTraining` ) SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`trainer` WHERE `userId` = ?);',
        [ trainer.id, trainer.maxTrainees, trainer.lastTraining, trainer.id ]
      );
      await connection.execute(
        'UPDATE `'+this.database+'`.`trainer` SET `maxTrainees` = ?, `lastTraining` = ? WHERE `userId` = ?;',
        [ trainer.maxTrainees, trainer.lastTraining, trainer.id ]
      );

      for (const trainee of trainer.trainees) {
        await connection.execute(
          'INSERT INTO `'+this.database+'`.`trainee` ( `id`, `xp`, `disposition`, `lastTraining` ) SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`trainee` WHERE `id` = ?);',
          [trainee.id, trainee.xp, trainee.disposition, trainee.lastTraining, trainee.id]
        );
        await connection.execute(
          'UPDATE `'+this.database+'`.`trainee` SET `xp` = ?, `disposition` = ?, `lastTraining` = ? WHERE `id` = ?;',
          [trainee.xp, trainee.disposition, trainee.lastTraining, trainee.id]
        );

        await connection.execute(
          'INSERT INTO `'+this.database+'`.`training` ( `trainerId`, `traineeId` ) SELECT ?, ? WHERE NOT EXISTS (SELECT 0 FORM `'+this.database+'`.`training` WHERE `traineeId` = ?);',
          [trainer.id, trainee.id, trainee.id]
        );
        await connection.execute(
          'UPDATE `'+this.database+'`.`training` SET `trainerId` = ? WHERE `traineeId` = ?;', [trainer.id, trainee.id]
        );
      }
      await connection.commit();
    }
    catch (err) {
      await connection.rollback();
    }
    finally {
      await connection.release();
    }
  }
}