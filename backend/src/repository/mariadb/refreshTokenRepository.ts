import IRefreshTokenRepository from '../../domain/entities/refreshTokenAggregate/IRefreshTokenRepository';
import RefreshToken from '../../domain/entities/refreshTokenAggregate/RefreshToken';
import mariadb from "mariadb";

export class RefreshTokenRepository implements IRefreshTokenRepository {
  private constructor(
    private readonly pool: mariadb.Pool,
    private readonly database: string
  ) {}

  public static async createWithPool(pool: mariadb.Pool, database: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const connection = await pool.getConnection();
    try {
      // TODO: For the future we should also handle schema updates, for now: just ensure the tables are thereCREATE TABLE refresh_tokens (
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`refreshToken` ( `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, `token` varchar(255) NOT NULL UNIQUE, `revoked` bit DEFAULT 0, `expiresAt` datetime NOT NULL, `userId` int NOT NULL REFERENCES `'+database+'`.`user` ( `id` ) );');
    }
    finally {
      await connection.release();
    }

    return new RefreshTokenRepository(pool, database);
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
    return RefreshTokenRepository.createWithPool(pool, database);
  }

  async create(refreshToken: RefreshToken): Promise<void> {
    const connection = await this.pool.getConnection();
    try {
      await connection.query(
        'INSERT INTO `'+this.database+'`.`refreshToken` (`token`, `userId`, `expiresAt`, `revoked`) VALUES (?, ?, ?, ?);',
        [ refreshToken.token, refreshToken.userId, refreshToken.expiresAt, refreshToken.revoked ]
      );
    }
    finally {
      await connection.release();
    }
  }

  async findByToken(token: string): Promise<RefreshToken | null> {
    const connection = await this.pool.getConnection();
    try {
      const [rows] = await connection.query(
        'SELECT * FROM `'+this.database+'`.`refreshToken` WHERE `token` = ? LIMIT 1;',
        [ token ]
      );
      const data = (rows as any[])[0];
      if (!data) return null;
      return new RefreshToken(
        data.token,
        data.user_id,
        data.expires_at,
        !!data.revoked
      );
    }
    finally {
      await connection.release();
    }
  }

  async revokeToken(token: string): Promise<void> {
    const connection = await this.pool.getConnection();
    try {
      await connection.query(
        'UPDATE `'+this.database+'`.`refreshToken` SET `revoked` = 1 WHERE `token` = ?;',
        [ token ]
      );
    }
    finally {
      await connection.release();
    }
  }

  async removeToken(token: string): Promise<void> {
    const connection = await this.pool.getConnection();
      try {
      await connection.query('DELETE FROM `'+this.database+'`.`refreshToken` WHERE `token` = ?;', [ token ]);
    }
    finally {
      await connection.release();
    }
  }
}
