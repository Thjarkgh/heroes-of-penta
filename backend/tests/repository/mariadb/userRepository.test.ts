// import { describe, it, expect, beforeAll, afterAll } from 'vitest';
// import mariadb from 'mariadb';
// import UserRepository from "../../../src/repository/mariadb/userRepository"
// import { config } from 'dotenv';
// config({ path: '../.env', debug: true });

// describe('UserRepository', () => {
//   let repoPool: mariadb.Pool;
//   let userRepository: UserRepository;

//   beforeAll(async () => {
//     const rootConfig = {
//       host: process.env["MYSQL_HOST"],
//       user: 'root',
//       password: process.env["MYSQL_ROOT_PASSWORD"],
//       port: Number.parseInt(process.env["MYSQL_PORT"]!),
//       connectionLimit: 1
//     };
//     const pool = mariadb.createPool(rootConfig);

//     console.log("Creating test database");
//     await pool.execute('CREATE DATABASE IF NOT EXISTS `' + process.env["MYSQL_TEST_DATABASE"] + '` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;');
//     console.log("Creating test user");
//     await pool.execute("CREATE OR REPLACE USER '" + process.env["MYSQL_TEST_USER"] + "' IDENTIFIED BY '" + process.env["MYSQL_TEST_PASSWORD"] + "'");
//     console.log("Granting privileges to test user");
//     await pool.execute('GRANT ALL PRIVILEGES ON `' + process.env["MYSQL_TEST_DATABASE"] + "`.* TO '" + process.env["MYSQL_TEST_USER"] + "';");
//     console.log("Flushing privileges");
//     await pool.execute('FLUSH PRIVILEGES;');
//     await pool.end();

//     const testUserConfig = {
//       host: process.env["MYSQL_HOST"],
//       user: process.env["MYSQL_TEST_USER"],
//       password: process.env["MYSQL_TEST_PASSWORD"],
//       port: Number.parseInt(process.env["MYSQL_PORT"]!),
//       database: process.env["MYSQL_TEST_DATABASE"],
//       //debug: true,
//       connectionLimit: 50
//     };
//     repoPool = mariadb.createPool(testUserConfig);

//     console.log("Creating UserRepository");
//     userRepository = await UserRepository.createWithPool(repoPool, process.env["MYSQL_TEST_DATABASE"]!);
//   });

//   afterAll(async () => {
//     await repoPool.end();
//   });

//   it('should create a user', async () => {
//     const user = await userRepository.createNewUser();
//     expect(user).toBeDefined();
//     expect(user.id).toBeDefined();
//     expect(user.instagramAccounts).to.be.empty;
//     expect(user.tikTokAccount).to.be.null;
//   });
// });