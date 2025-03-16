import User from "../../domain/entities/userAggregate/User";
import IUserRepository from "../../domain/entities/userAggregate/IUserRepository";
import mariadb from "mariadb";
// import Wallet from "../../domain/entities/walletAggregate/Wallet";
import InstagramAccount from "../../domain/entities/userAggregate/InstagramAccount";
import TikTokAccount from "../../domain/entities/userAggregate/TikTokAccount";

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
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`instagramAccount` ( `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY, `userId` int not null REFERENCES `'+database+'`.`user` ( `id` ), `instagramUserId` varchar(128) NOT NULL UNIQUE, `token` varchar(512) null, `expiry` datetime null );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`tiktokAccount` ( `tikTokId` varchar(128) NOT NULL PRIMARY KEY, `userId` int not null UNIQUE REFERENCES `'+database+'`.`user` ( `id` ), `accessToken` varchar(512) NOT NULL, `accessTokenExpiry` int NOT NULL, `refreshToken` varchar(512) NOT NULL, `refreshTokenExpiry` int NOT NULL );');
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`wallet` ( `address` char(42) NOT NULL PRIMARY KEY, `userId` int NOT NULL UNIQUE REFERENCES `'+database+'`.`user` ( `id` ) );');

      const dummyUserId = process.env["DUMMY_USER_ID"];
      const existingDummyUser = await connection.query('SELECT * FROM `'+database+'`.`user` WHERE `id` = ?;', [dummyUserId]);
      if (existingDummyUser.length < 1) {
        await connection.execute(
          'INSERT INTO `'+database+'`.`user` (`id`, `acceptedTermsOnce`, `acceptedTerms`, `acceptedPrivacyOnce`, `acceptedPrivacy`) VALUES (?, ?, ?, ?, ?);',
          [dummyUserId, 0, 0, 0, 0]
        );
      }
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
      return new User(id, [], null);
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

  // async findByWalletAddress(address: string): Promise<User | undefined> {
  //   const connection = await this.pool.getConnection();
  //   try {
  //     const data = await connection.query('SELECT `userId` from `'+this.database+'`.`wallet` WHERE `address` = ?;', [address]);
  //     if (data.length === 0) {
  //       return undefined;
  //     } else if (data.length !== 1) {
  //       throw new Error(`Should have found max one wallet for address ${address}`);
  //     } else {
  //       const userId = data[0].userId;
  //       return await this.getByUserId(connection, userId);
  //     }
  //   }
  //   finally {
  //     await connection.release();
  //   }
  // }

  async findByTikTokId(id: string): Promise<User | undefined> {
    const connection = await this.pool.getConnection();
    try {
      const data = await connection.query('SELECT `userId` from `'+this.database+'`.`tiktokAccount` WHERE `tikTokId` = ?;', [id]);
      if (data.length === 0) {
        return undefined;
      } else if (data.length !== 1) {
        throw new Error(`Should have found max one TikTok account for id ${id}`);
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
    const instagramAccounts = await connection.query<{id:number,instagramUserId:string,token:string|null|undefined,expiry:Date|null|undefined}[]>(
      'SELECT `id`, `instagramUserId`, `token`, `expiry` from `'+this.database+'`.`instagramAccount` WHERE `userId` = ?;',
      [userId]
    );
    const tikTokAccount = await connection.query<{tikTokId:string,accessToken:string,accessTokenExpiry:number,refreshToken:string,refreshTokenExpiry:number}[]>(
      'SELECT `tikTokId`, `accessToken`, `accessTokenExpiry`, `refreshToken`, `refreshTokenExpiry` from `'+this.database+'`.`tiktokAccount` WHERE `userId` = ?;',
      [userId]
    );
    if (tikTokAccount.length > 1) {
      throw new Error(`Found multiple TikTok accounts for user ${userId}`);
    }
    // const wallets = await connection.query<{id:number,address:string}[]>('SELECT `id`, `address` from `'+this.database+'`.`wallet` WHERE `userId` = ?;', [userId]);

    const user = new User(
      userId,
      // wallets.map((w) => new Wallet(w.address)),
      instagramAccounts.map((a) => new InstagramAccount(a.instagramUserId, a.token || undefined, a.expiry || undefined)),
      tikTokAccount.length === 1 ? new TikTokAccount(tikTokAccount[0].tikTokId, tikTokAccount[0].accessToken, tikTokAccount[0].accessTokenExpiry, tikTokAccount[0].refreshToken, tikTokAccount[0].refreshTokenExpiry) : null
    );
    return user;
  }

  async save(user: User): Promise<void> {
    const connection = await this.pool.getConnection();
    try {
      // since the only way to create a user should be by calling this.createNewUser => no need to check if exist, bc if not, that IS an error
      const existing = await this.getByUserId(connection, user.id);

      // const missingWallets = user.wallets.filter((w) => !existing.wallets.find((ew) => ew.address === w.address));
      const missingInstagramAccounts = user.instagramAccounts.filter((ia) => !existing.instagramAccounts.find((eia) => eia.id === ia.id));
      const dirtyInstagramAccounts = user.instagramAccounts.filter((ia) => existing.instagramAccounts.find((eia) => eia.id === ia.id && (eia.longLivedToken !== ia.longLivedToken || eia.expiry !== ia.expiry)));

      // if (missingWallets.length > 0) {
      //   await connection.batch('INSERT INTO `'+this.database+'`.`wallet` (`userId`, `address`) VALUES (?, ?);', missingWallets.map((w) => [user.id, w.address]));
      // }


      if (missingInstagramAccounts.length > 0) {
        await connection.batch(
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

      await this.setOptionalUniqueUserReferencedObject(
        existing.tikTokAccount,
        user.tikTokAccount,
        (id) => connection.query('DELETE FROM `'+this.database+'`.`tiktokAccount` WHERE `tikTokId` = ?;', [id]),
        (p) => connection.query(
          'INSERT INTO `'+this.database+'`.`tiktokAccount` ( `tikTokId`, `accessToken`, `accessTokenExpiry`, `refreshToken`, `refreshTokenExpiry` ) VALUES ( ?, ?, ?, ?, ? );',
          [p.id, p.accessToken, p.accessTokenExpiry, p.refreshToken, p.refreshTokenExpiry]
        ),
        (p) => connection.query(
          'UPDATE `'+this.database+'`.`tiktokAccount` SET `accessToken` = ?, `accessTokenExpiry` = ?, `refreshToken` = ?, `refreshTokenExpiry` = ? WHERE `tikTokId` = ?;',
          [p.accessToken, p.accessTokenExpiry, p.refreshToken, p.refreshTokenExpiry, p.id]
        )
      );
    }
    finally {
      await connection.release();
    }
  }

  private async setOptionalUniqueUserReferencedObject<T extends string | number, U extends { id: T, sameAs(other: U): boolean }>(
    oldProperty: U | null,
    newProperty: U | null,
    removeProperty: (id: T) => Promise<void>,
    insertProperty: (p: U) => Promise<void>,
    updateProperty: (p: U) => Promise<void>
  ) {

    if (newProperty === null) {
      if (oldProperty != null) {
        await removeProperty(oldProperty.id); // remove old property
      }
    } else {
      if (oldProperty == null) {
        // user added ref-obj in this action => delete old mapping! (in case the same ref-obj was already mapped to some other user)
        await removeProperty(newProperty.id);
        await insertProperty(newProperty);
      } else {
        if (oldProperty.id !== newProperty.id) {
          // user changed their ref-obj completely => delete mapping of existing user (that ref-obj is no longer registered)
          // and ensure the new ref-obj does not exist yet (in case it is mapped to another user)
          //await connection.query('DELETE `'+this.database+'`.`tiktokAccount` WHERE `tikTokId` IN ( ?, ? );', [oldProperty.id, newProperty.id]);
          await removeProperty(oldProperty.id);
          await removeProperty(newProperty.id);
          // then add new mapping!
          await insertProperty(newProperty);
        } else {
          // id has not changed (ref-obj is the same), check if some properties of the reference need to be updated
          if (!newProperty.sameAs(oldProperty)) {
            await updateProperty(newProperty)
          }
        }
      }
    }
  }
}