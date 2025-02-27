import IMentionRepository from "../../service/IMentionRepository";
import mariadb from "mariadb";
import Mention from "../../domain/entities/mentionAggregate/Mention";

export default class MentionsRepository implements IMentionRepository {
  private constructor(
    private readonly pool: mariadb.Pool,
    private readonly database: string
  ) {}

  public static async createWithPool(pool: mariadb.Pool, database: string) {
    if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
    const connection = await pool.getConnection();
    try {
      // TODO: For the future we should also handle schema updates, for now: just ensure the tables are there
      await connection.execute('CREATE TABLE IF NOT EXISTS `'+database+'`.`mention` ( `id` bigint not null AUTO_INCREMENT PRIMARY KEY, `mediaId` varchar(256) NOT NULL, `commentId` varchar(256) NOT NULL, `timestamp` int NOT NULL );');
    }
    finally {
      await connection.release();
    }

    return new MentionsRepository(pool, database);
  }

  async push(m: Mention): Promise<void> {
    const connection = await this.pool.getConnection();
    try {
      await connection.execute('INSERT INTO `'+this.database+'`.mention` (`mediaId`, `commentId`, `timestamp`) VALUES (?, ?);', [m.mediaId, m.commentId, m.timestamp]);
    }
    finally {
      await connection.release();
    }
  }
}