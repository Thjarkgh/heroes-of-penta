import User from "../../domain/entities/userAggregate/User";
import IUserRepository from "../../domain/entities/userAggregate/IUserRepository";
import mariadb from "mariadb";
import Wallet from "../../domain/entities/userAggregate/Wallet";
import InstagramAccount from "../../domain/entities/userAggregate/InstagramAccount";

export default class UserRepository implements IUserRepository {
  private constructor(
    private readonly pool: mariadb.Pool,
    private readonly database: string
  ) {}

  public static async createWithPool(pool: mariadb.Pool, database: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const connection = await pool.getConnection();
    try {
      // TODO: For the future we should also handle schema updates, for now: just ensure the tables are there
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`user` ( `id` int not null AUTO_INCREMENT PRIMARY KEY, `acceptedTermsOnce` bit not null, `acceptedTerms` bit not null, `acceptedPrivacyOnce` bit not null, `acceptedPrivacy` bit not null );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`instagramAccount` ( `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY, `userId` int not null REFERENCES `'+database+'`.`user` ( `id` ), `instagramUserId` varchar(128) NOT NULL UNIQUE, `token` varchar(128) null, `expiry` datetime null );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`wallet` ( `id` int not null AUTO_INCREMENT PRIMARY KEY, `userId` int NOT NULL REFERENCES `'+database+'`.`user` ( `id` ), `address` char(42) NOT NULL );');
    }
    finally {
      await connection.release();
    }

    return new UserRepository(pool, database);
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
    return UserRepository.createWithPool(pool, database);
  }

  async createNewUser(): Promise<User> {
    const connection = await this.pool.getConnection();
    try {
      const result = await connection.query('INSERT INTO `'+this.database+'`.`user` ( `acceptedTermsOnce`, `acceptedTerms`, `acceptedPrivacyOnce`, `acceptedPrivacy` ) VALUES (0, 0 ,0 ,0);');
      const id = result.insertId;
      if (id == undefined) {
        throw new Error(`Failed to create new user in db`);
      }
      return new User(id, [], []);
    }
    finally {
      connection.release();
    }
  }

  async findByInstagramId(instagramUserId: string): Promise<User | undefined> {
    const connection = await this.pool.getConnection();
    try {
      const data = await connection.query('SELECT `userId` FROM `'+this.database+'`.`instagramAccount` WHERE `instagramUserId` = ?;', [instagramUserId]);
      if (data.length === 0) {
        return undefined;
      } else if (data.length !== 1) {
        throw new Error(`Should have found max one user for instagram user id ${instagramUserId}`);
      } else {
        const userId = data[0].userId;
        return await this.getByUserId(connection, userId);
      }
    }
    finally {
      await connection.release();
    }
  }

  async findByWalletAddress(address: string): Promise<User | undefined> {
    const connection = await this.pool.getConnection();
    try {
      const data = await connection.query('SELECT `userId` from `'+this.database+'`.`wallet` WHERE `address` = ?;', [address]);
      if (data.length === 0) {
        return undefined;
      } else if (data.length !== 1) {
        throw new Error(`Should have found max one wallet for address ${address}`);
      } else {
        const userId = data[0].userId;
        return await this.getByUserId(connection, userId);
      }
    }
    finally {
      await connection.release();
    }
  }

  async getById(id: number): Promise<User> {
    const connection = await this.pool.getConnection();
    try {
      return this.getByUserId(connection, id);
    }
    finally {
      await connection.release();
    }
  }

  async getByUserId(connection: mariadb.PoolConnection, userId: number): Promise<User> {
    const instagramAccounts = await connection.query<{id:number,instagramUserId:string,token:string|null|undefined,expiry:Date|null|undefined}[]>('SELECT `id`, `instagramUserId`, `token`, `expiry` from `'+this.database+'`.`instagramAccount` WHERE `userId` = ?;', [userId]);
    const wallets = await connection.query<{id:number,address:string}[]>('SELECT `id`, `address` from `'+this.database+'`.`wallet` WHERE `userId` = ?;', [userId]);

    const user = new User(userId, wallets.map((w) => new Wallet(w.address)), instagramAccounts.map((a) => new InstagramAccount(a.instagramUserId, a.token || undefined, a.expiry || undefined)));
    return user;
  }

  async save(user: User): Promise<void> {
    const connection = await this.pool.getConnection();
    try {
      // since the only way to create a user should be by calling this.createNewUser => no need to check if exist, bc if not, that IS an error
      const existing = await this.getByUserId(connection, user.id);

      const missingWallets = user.wallets.filter((w) => !existing.wallets.find((ew) => ew.address === w.address));
      const missingInstagramAccounts = user.instagramAccounts.filter((ia) => !existing.instagramAccounts.find((eia) => eia.id === ia.id));
      const dirtyInstagramAccounts = user.instagramAccounts.filter((ia) => existing.instagramAccounts.find((eia) => eia.id === ia.id && (eia.longLivedToken !== ia.longLivedToken || eia.expiry !== ia.expiry)));

      if (missingWallets.length > 0) {
        await connection.query('INSERT INTO `'+this.database+'`.`wallet` (`userId`, `address`) VALUES (?, ?);', missingWallets.map((w) => [user.id, w.address]));
      }
      if (missingInstagramAccounts.length > 0) {
        await connection.query(
          'INSERT INTO `'+this.database+'`.`instagramAccount` (`userId`, `instagramUserId`, `token`, `expiry`) VALUES (?, ?, ?, ?);',
          missingInstagramAccounts.map((a) => [user.id, a.id, a.longLivedToken, a.expiry])
        );
      }
      for (const dirty of dirtyInstagramAccounts) {
        await connection.query(
          'UPDATE `'+this.database+'`.`instagramAccount` SET `token` = ?, `expiry` = ? WHERE `instagramId` = ?;',
          [dirty.longLivedToken, dirty.expiry, dirty.id]
        );
      }
    }
    finally {
      await connection.release();
    }
  }
}