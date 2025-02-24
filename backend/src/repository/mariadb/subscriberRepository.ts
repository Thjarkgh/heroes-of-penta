import Subscriber from "../../domain/entities/subscriberAggregate/Subscriber";
import IRepository from "../../IRepository";
import mariadb from "mariadb";
import SubscriberSecret from "../../domain/entities/subscriberAggregate/SubscriberSecret";

export default class SubscriberRepository implements IRepository<Subscriber, string> {
  private constructor(
    private readonly pool: mariadb.Pool,
    private readonly database: string
  ) {}

  public static async createWithPool(pool: mariadb.Pool, database: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const connection = await pool.getConnection();
    try {
      // TODO: For the future we should also handle schema updates, for now: just ensure the tables are there
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`subscriber` ( `id` bigint not null AUTO_INCREMENT PRIMARY KEY, `email` varchar(256) NOT NULL UNIQUE KEY, `confirmed` bit NOT NULL );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`subscriberSecretPurpose` ( `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY, `purpose` varchar(128) NOT NULL UNIQUE );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`subscriberSecret` ( `id` bigint not null AUTO_INCREMENT PRIMARY KEY, `subscriberId` bigint NOT NULL REFERENCES `'+database+'`.`subscriber` ( `id` ), `purposeId` int NOT NULL REFERENCES `'+database+'`.`subscriberSecretPurpose` ( `id` ), `validUntil` datetime NOT NULL, `secret` char(64) NOT NULL );');
    }
    finally {
      await connection.release();
    }

    return new SubscriberRepository(pool, database);
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
    return SubscriberRepository.createWithPool(pool, database);
  }

  async get(email: string): Promise<Subscriber> {
    const connection = await this.pool.getConnection();
    try {
      const subscriberResultSets = await connection.query('SELECT `id`, `confirmed` FROM `'+this.database+'`.`subscriber` WHERE `email` = ?;', [email]);
      if (subscriberResultSets.length !== 1) throw new Error(`Should have found one subscriber for email ${email}`);
      const id = subscriberResultSets[0].id;
      const isConfirmed = subscriberResultSets[0].confirmed;

      const secretsResultSets = await connection.query('SELECT `p`.`purpose`, `s`.`secret`, `s`.`validUntil` FROM `'+this.database+'`.`subscriberSecret` AS `s` JOIN `'+this.database+'`.`subscriberSecretPurpose` AS `p` ON `p`.`id` = `s`.`purposeId` WHERE `s`.`subscriberId` = ?;', [id]);
      const secrets = secretsResultSets.map((s: any) => ({ purpose: s.purpose, secret: SubscriberSecret.loadSecret(s.validUntil, s.secret)}));

      return Subscriber.createExisting(email, isConfirmed, secrets);
    }
    finally {
      await connection.release();
    }
  }

  async has(email: string) {
    const connection = await this.pool.getConnection();
    try {
      const resultSets = await connection.query('SELECT 0 FROM `'+this.database+'`.`subscriber` WHERE `email` = ?;', [email]);
      if (resultSets.length > 1) throw new Error("email unique constraint violated!");
      return resultSets.length === 1;
    }
    finally {
      await connection.release();
    }
  }

  async save(subscriber: Subscriber) {
    const connection = await this.pool.getConnection();
    try {
      try {
        await connection.beginTransaction();
        await connection.execute(
          'INSERT INTO `'+this.database+'`.`subscriber` ( `email`, `confirmed` ) SELECT ?, ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`subscriber` WHERE `email` = ?);',
          [ subscriber.email, subscriber.isConfirmed(), subscriber.email]
        );
        await connection.execute('UPDATE `'+this.database+'`.`subscriber` SET `confirmed` = ? WHERE `email` = ?;', [ subscriber.isConfirmed(), subscriber.email ]);
        
        const getIdRS = await connection.query('SELECT `id` FROM `'+this.database+'`.`subscriber` WHERE `email` = ?;', [ subscriber.email ]);
        if (getIdRS.length !== 1) throw new Error(`One select needs to yield one result set!`);
        const subscriberId = getIdRS[0].id;

        const secrets = subscriber.getSecrets();
        if (secrets.length > 0) {
          const purposes = secrets.map((s) => s.purpose);
          const purposeSet = new Set(purposes);
          const uniquePurposes = new Array(...purposeSet.keys());
          await Promise.all(uniquePurposes.map(async (purpose) => {
            return connection.execute('INSERT INTO `'+this.database+'`.`subscriberSecretPurpose` ( `purpose` ) SELECT ? WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`subscriberSecretPurpose` WHERE `purpose` = ?);', [ purpose, purpose ]);
          }));
          const subscriberSecrets: [ string, Date, string ][] = secrets.map((s) => ([ s.purpose, s.secret.validUntil, s.secret.secret ]));
          
          await connection.execute('CREATE TEMPORARY TABLE `nss` ( `purpose` varchar(128) NOT NULL, `validUntil` datetime NOT NULL, `secret` char(64) NOT NULL ); ');
          await connection.batch('INSERT INTO `nss` ( `purpose`, `validUntil`, `secret` ) VALUES ( ?, ?, ?); ', subscriberSecrets);
          await connection.execute(
            'DELETE `s` FROM `'+this.database+'`.`subscriberSecret` AS `s` JOIN `'+this.database+'`.`subscriberSecretPurpose` AS `p` ON `p`.`id` = `s`.`purposeId` ' +
            'WHERE `s`.`subscriberId` = ? ' +
            'AND NOT EXISTS (SELECT 0 FROM `nss` WHERE `nss`.`secret` = `s`.`secret` AND `nss`.`purpose` = `p`.`purpose`);', [ subscriberId ]);
          await connection.execute(
            'UPDATE `'+this.database+'`.`subscriberSecret` AS `s` ' +
            'JOIN `'+this.database+'`.`subscriberSecretPurpose` AS `p` ON `p`.`id` = `s`.`purposeId` ' +
            'JOIN `nss` ON `nss`.`secret` = `s`.`secret` AND `nss`.`purpose` = `p`.`purpose` ' +
            'SET `s`.`validUntil` = `nss`.`validUntil` '+
            'WHERE `s`.`subscriberId` = ? AND `s`.`validUntil` <> `nss`.`validUntil`;', [ subscriberId ]);
          await connection.execute(
            'INSERT INTO `'+this.database+'`.`subscriberSecret` ( `subscriberId`, `purposeId`, `validUntil`, `secret` ) ' +
            'SELECT ?, `p`.`id`, `nss`.`validUntil`, `nss`.`secret` ' +
            'FROM `nss` JOIN `'+this.database+'`.`subscriberSecretPurpose` AS `p` ON `p`.`purpose` = `nss`.`purpose` ' +
            'WHERE NOT EXISTS (SELECT 0 FROM `'+this.database+'`.`subscriberSecret` AS `s` WHERE `s`.`subscriberId` = ? AND `s`.`purposeId` = `p`.`id` AND `s`.`secret` = `nss`.`secret`); ',
            [ subscriberId, subscriberId ]
          );
          await connection.execute('DROP TEMPORARY TABLE `nss`;');
        } else {
          await connection.execute('DELETE FROM `'+this.database+'`.`subscriberSecret` WHERE `subscriberId` = ?;', [ subscriberId ]);
        }
        await connection.commit();
        return subscriber.email;
      }
      catch (err) {
        await connection.rollback();
        throw err;
      }
    }
    finally {
      await connection.release();
    }
  }

  async create(email: string) {
    const subscriber = Subscriber.createNew(email);
    const connection = await this.pool.getConnection();
    try {
      await connection.execute(
        'INSERT INTO `'+this.database+'`.`subscriber` ( `email`, `confirmed` ) VALUES ( ?, ? );',
        [ subscriber.email, subscriber.isConfirmed() ]
      );
      return subscriber;
    }
    finally {
      await connection.release(); 
    }
  }

  async delete(subscriber: Subscriber) {
    const connection = await this.pool.getConnection();
    try {
      await connection.beginTransaction();
      try {
        await connection.execute('DELETE FROM `'+this.database+'`.`subscriberSecret` AS `ss` WHERE `ss`.`subscriberId` IN (SELECT `s`.`id` FROM `'+this.database+'`.`subscriber` AS `s` WHERE `s`.`email` = ?;', [ subscriber.email ]);
        await connection.execute('DELETE FROM `'+this.database+'`.`subscriber` WHERE `email` = ?;', [ subscriber.email ]);
        await connection.commit();
      }
      catch (err) {
        await connection.rollback();
      }
    }
    finally {
      await connection.release();
    }
  }
}