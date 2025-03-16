import Trainee from "../../domain/entities/heroAggregate/Trainee";
import mariadb from "mariadb";
import Trainer from "../../domain/entities/heroAggregate/Trainer";
import ITrainerRepository from "../../domain/entities/heroAggregate/ITrainerRepository";
import Disposition from "../../domain/entities/heroAggregate/Disposition";

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
      // trainerId = userId
      // traineeId = fletchlingId
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`trainer` ( `userId` int not null PRIMARY KEY REFERENCES `'+database+'`.`user` (`id`), `maxTrainees` int not null, `lastTraining` int not null, `disposition` varchar(2048) not null, `xp` int not null, `xp` int not null, `leftoverXp` int not null );');
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
      const trainerData = await connection.query<{maxTrainees:number,lastTraining:number,disposition:string,xp:number,leftoverXp:number}[]>(
        'SELECT `maxTrainees`, `lastTraining`, `disposition`, `xp`, `leftoverXp` FROM `'+this.database+'`.`trainer` WHERE `userId` = ?;', [userId]
      );
      if (trainerData.length > 1) {
        throw new Error(`Database corrupted: multiple trainers for one user (${userId})`);
      }
      if (trainerData.length < 1) {
        const newTrainerData = await connection.query(
          'INSERT INTO `'+this.database+'`.`trainer` (`userId`,`maxTrainees`,`lastTraining`, `disposition`, `xp`, `leftoverXp`) VALUES (?, 1, 0, ?, 0, 0);', [userId, ""]
        );
        trainerData.push({ maxTrainees: 1, lastTraining: 0, disposition: "{}", xp: 0, leftoverXp: 0 });
      }

      const traineeData = await connection.query<{id:number,xp:number,disposition:string,lastTraining:number}[]>(
        'SELECT `f`.`id`, `f`.`xp`, `f`.`disposition`, `f`.`lastTraining` '+
        'FROM `'+this.database+'`.`training` AS `t` '+
        'JOIN `'+this.database+'`.`trainee` AS `f` ON `f`.`id` = `t`.`traineeId` '+
        'WHERE `t`.`trainerId` = ?;',
        [userId]
      );
      const trainees: Trainee[] = traineeData.map((d) => {
        const dispoData: {[key:string]:number} = (d.disposition || "") == "" ? {} : JSON.parse(d.disposition);
        const dispo = new Map<string, number>(Object.entries(dispoData));
        return new Trainee(d.id, d.xp, new Disposition(dispo), d.lastTraining);
      });
      const trainerDispo = (trainerData[0].disposition || "") == "" ? {} : JSON.parse(trainerData[0].disposition);
      const trainer = new Trainer(
        userId,
        trainerData[0].maxTrainees,
        trainerData[0].lastTraining,
        trainees,
        trainerData[0].xp,
        trainerData[0].leftoverXp,
        new Disposition(new Map(Object.entries(trainerDispo)))
      );
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
      console.log(`Trainer: id: ${trainer.id}, maxTrainees: ${trainer.maxTrainees}, lastTraining: ${trainer.lastTraining}, xp: ${trainer.trainerXP}, leftoverXP: ${trainer.leftoverXP}, dispo: ${JSON.stringify(Object.fromEntries(trainer.disposition.disposition.entries()))}`);
      await connection.execute(
        'INSERT INTO `'+this.database+'`.`trainer` ( `userId`, `maxTrainees`, `lastTraining`, `disposition`, `xp`, `leftoverXp` ) SELECT ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`trainer` WHERE `userId` = ?);',
        [ trainer.id, trainer.maxTrainees, trainer.lastTraining, JSON.stringify(Object.fromEntries(trainer.disposition.disposition.entries())), trainer.trainerXP, trainer.leftoverXP, trainer.id ]
      );
      console.log(`Update`)
      await connection.execute(
        'UPDATE `'+this.database+'`.`trainer` SET `maxTrainees` = ?, `lastTraining` = ?, `xp` = ?, `leftoverXp` = ?, `disposition` = ? WHERE `userId` = ?;',
        [ trainer.maxTrainees, trainer.lastTraining, trainer.trainerXP, trainer.leftoverXP, JSON.stringify(Object.fromEntries(trainer.disposition.disposition.entries())), trainer.id ]
      );

      for (const trainee of trainer.trainees) {
        console.log(`Trainee: id: ${trainee.id}, xp: ${trainee.xp}, lastTraining: ${trainee.lastTraining}, dispo: ${JSON.stringify(Object.fromEntries(trainee.disposition.disposition.entries()))}`);
        await connection.execute(
          'INSERT INTO `'+this.database+'`.`trainee` ( `id`, `xp`, `disposition`, `lastTraining` ) SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`trainee` WHERE `id` = ?);',
          [trainee.id, trainee.xp, JSON.stringify(Object.fromEntries(trainee.disposition.disposition.entries())), trainee.lastTraining, trainee.id]
        );
        console.log(`Update`);
        await connection.execute(
          'UPDATE `'+this.database+'`.`trainee` SET `xp` = ?, `disposition` = ?, `lastTraining` = ? WHERE `id` = ?;',
          [trainee.xp, JSON.stringify(Object.fromEntries(trainee.disposition.disposition.entries())), trainee.lastTraining, trainee.id]
        );

        // TODO: make this more efficient
        console.log(`Training: trainerId: ${trainer.id}, traineeId: ${trainee.id}`);
        await connection.execute(
          'DELETE FROM `'+this.database+'`.`training` WHERE `trainerId` = ? OR `traineeId` = ?;'
          [trainer.id, trainee.id]
        );
        console.log(`insert`);
        await connection.execute(
          'INSERT INTO `'+this.database+'`.`training` ( `trainerId`, `traineeId` ) VALUES ( ?, ? );', // SELECT ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`training` WHERE `traineeId` = ?);',
          [trainer.id, trainee.id] //, trainee.id]
        );
        // console.log(`update`);
        // await connection.execute(
        //   'UPDATE `'+this.database+'`.`training` SET `trainerId` = ? WHERE `traineeId` = ?;', [trainer.id, trainee.id]
        // );
      }
      await connection.commit();
    }
    catch (err) {
      console.log(err);
      await connection.rollback();
      throw err;
    }
    finally {
      await connection.release();
    }
  }

  async findTrainee(id: number): Promise<Trainee | undefined> {
    const connection = await this.pool.getConnection();
    try {
      const traineeData = await connection.query<{id:number,xp:number,disposition:string,lastTraining:number}[]>(
        'SELECT `f`.`id`, `f`.`xp`, `f`.`disposition`, `f`.`lastTraining` '+
        'FROM `'+this.database+'`.`trainee` AS `f` '+
        'WHERE `f`.`id` = ?;',
        [id]
      );
      if (traineeData.length > 1) {
        throw new Error(`Corrupt database, got duplicate trainees for ${id}`);
      }
      if (traineeData.length < 1) {
        return undefined;
      }

      const dispoData: {[key:string]:number} = (traineeData[0].disposition || "") == "" ? {} : JSON.parse(traineeData[0].disposition);
      const dispo = new Map<string, number>(Object.entries(dispoData));
      return new Trainee(traineeData[0].id, traineeData[0].xp, new Disposition(dispo), traineeData[0].lastTraining);
    }
    finally {
      await connection.release();
    }
  }
}