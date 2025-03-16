import express from "express";
import cors from "cors";
import compression from "compression";
import cookieParser from "cookie-parser";
import http from "http";
import path from 'path';
import { getBaseDir } from "./helper/getBaseDir";
import { config } from "dotenv";
import SubscriberController from "./presentation/controller/SubscriberController";
import SubscriberService from "./service/SubscriberService";
import SmtpService from "./adapter/SmtpService";
import SubscriberRepository from "./repository/mariadb/subscriberRepository";
import mariadb from "mariadb";
import MentionsService from "./service/MentionsService";
import InstagramMentionsController from "./presentation/controller/InstagramMentionsController";
import { AuthController } from "./presentation/controller/AuthController";
import AuthService from "./service/AuthService";
import { RefreshTokenRepository } from "./repository/mariadb/refreshTokenRepository";
import { UserController } from "./presentation/controller/UserController";
import UserService from "./service/UserService";
import InstagramAuthAdapter from "./adapter/InstagramAuthAdapter";
import UserRepository from "./repository/mariadb/userRepository";
import TrainerRepository from "./repository/mariadb/trainerRepository";
import OpenAiAdapter from "./adapter/OpenAiAdapter";
import InstagramMessageAdapter from "./adapter/InstagramMessageAdapter";
import { readFileSync } from "fs";
import TikTokAuthAdapter from "./adapter/TikTokAuthAdapter";
import WalletRepository from "./repository/web3/WalletRepository";
import FletchlingRepository from "./repository/web3/FletchlingRepository";
import { DataDeletionController } from "./presentation/controller/DataDeletionController";
import TrainingController from "./presentation/controller/TrainingController";
import TrainingService from "./service/TrainingService";

const start = async () => {
  config();

  const app = express();
  app.use(compression());
  app.use(cors());
  app.use(cookieParser());
  app.use(express.json({
    limit: '1mb',
    verify: (req, res, buffer) => {
      (req as any).rawBody = buffer;
    }
  }));  // for parsing application/json
  app.use(express.urlencoded({ extended: true, limit: '1mb' })) //for parsing application/x-www-form-urlencoded

  const basepath = getBaseDir(__dirname);
  app.use('/favicon.png', express.static(path.resolve(basepath, 'favicon.png')));
  app.use('/style.css', express.static(path.resolve(basepath, 'style.css')));
  app.use('/assets', express.static(path.resolve(basepath, 'assets')));
  app.use('/index.html', express.static(path.resolve(basepath, 'index.html')));
  app.use('/privacy.html', express.static(path.resolve(__dirname, '..', 'static', 'privacy.html')));
  app.use('/terms.html', express.static(path.resolve(__dirname, '..', 'static', 'terms.html')));
  
  //app.use('/.well-known/acme-challenge', express.static(path.resolve(basepath, '..', 'certificates', 'acme-challenge')))
  app.get('/.well-known/:folder/:file', (req, res) => {
    if (req.params.folder.includes('..') || req.params.file.includes('..')) {
      res.sendStatus(404);
    } else {
      res.sendFile(path.resolve(basepath, '..', 'certificates', req.params.folder, req.params.file));
    }
  });
  app.get('/.well-known/:file', (req, res) => {
    if (req.params.file.includes('..')) {
      res.sendStatus(404);
    } else {
      res.sendFile(path.resolve(basepath, '..', 'certificates', req.params.file));
    }
  });

  // DI

  app.set('views', path.join(__dirname, '..', 'views'));
  const getEnvVarOrThrow = (x: string) => {
    const val = process.env[x];
    if (val == undefined) {
      throw new Error(`not defined env var: ${x}`);
    }
    return val;
  };
  const smtpHost = getEnvVarOrThrow("SMTP_HOST");
  const smtpPort = Number.parseInt(getEnvVarOrThrow("SMTP_PORT"));
  const smtpUser = getEnvVarOrThrow("SMTP_USER");
  const smtpPassword = getEnvVarOrThrow("SMTP_PASSWORD");
  const smtpSender = getEnvVarOrThrow("SMTP_FROM");
  const templatesFolder = path.join(__dirname, "..", "views");

  const database = getEnvVarOrThrow("MYSQL_DATABASE");
  if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
  const pool = mariadb.createPool({
    host: getEnvVarOrThrow("MYSQL_HOST"),
    user: getEnvVarOrThrow("MYSQL_USER"),
    password: getEnvVarOrThrow("MYSQL_PASSWORD"),
    database,
    port: Number.parseInt(getEnvVarOrThrow("MYSQL_PORT")),
    connectionLimit: 50
  });

  // Subscriber (Newsletter)
  const smtpClient = new SmtpService(smtpHost, smtpPort, smtpUser, smtpPassword, smtpSender, templatesFolder);
  const subscriberRepository = await SubscriberRepository.createWithPool(pool, database);
  const subscriberService = new SubscriberService(subscriberRepository, smtpClient);
  const subscriberController = new SubscriberController(subscriberService);

  // User
  const userInstagramAppId = getEnvVarOrThrow("INSTAGRAM_USER_APP_ID");
  const userInstagramAppSecret = getEnvVarOrThrow("INSTAGRAM_USER_APP_SECRET");
  const userInstagramAppRedirectUri = getEnvVarOrThrow("INSTAGRAM_USER_APP_REDIRECTURI");
  const instagramAdapter = new InstagramAuthAdapter(userInstagramAppId, userInstagramAppSecret, userInstagramAppRedirectUri);
  const tikTokAdapter = new TikTokAuthAdapter(getEnvVarOrThrow("TIKTOK_CLIENT_KEY"), getEnvVarOrThrow("TIKTOK_CLIENT_SECRET"), "https://heroesofpenta.com/auth/login/tiktok");
  const userRepo = await UserRepository.createWithPool(pool, database);
  const walletRepo = new WalletRepository("https://sepolia-rpc.scroll.io/", `0x${getEnvVarOrThrow("ACCOUNT_REGISTER_ADDRESS")}`);
  const fletchlingRepo = new FletchlingRepository("https://sepolia-rpc.scroll.io/", `0x${getEnvVarOrThrow("SERVER_WALLET_PK")}`, `0x${getEnvVarOrThrow("FLETCHLING_NFT_ADDRESS")}`);
  const userService = new UserService(userRepo, instagramAdapter, tikTokAdapter, walletRepo, fletchlingRepo);
  const userController = new UserController(userService);

  // Mentions
  //const mentionsRepository = await MentionsRepository.createWithPool(pool, database);
  const trainerRepo = await TrainerRepository.createWithPool(pool, database);
  const openAI = OpenAiAdapter.build(getEnvVarOrThrow("OPENAI_API_KEY"));
  const instagramMessenger = new InstagramMessageAdapter(getEnvVarOrThrow("INSTAGRAM_USER_ID"), getEnvVarOrThrow("INSTAGRAM_USER_TOKEN"));
  const query = readFileSync(getEnvVarOrThrow("QUERY_FILE_PATH"), "utf8");
  const phrases = JSON.parse(readFileSync(getEnvVarOrThrow("DISPO_FILE_PATH"), "utf8"));
  const mentionsService = new MentionsService(trainerRepo, userRepo, openAI, instagramMessenger, query);
  const appSecret = getEnvVarOrThrow("INSTAGRAM_APP_SECRET");
  const msgSecret = getEnvVarOrThrow("INSTAGRAM_MSG_SECRET");
  const mentionsController = new InstagramMentionsController(mentionsService, appSecret, msgSecret);

  // Refresh
  const refreshTokenRepo = await RefreshTokenRepository.createWithPool(pool, database);
  const authService = new AuthService(userRepo, refreshTokenRepo, getEnvVarOrThrow("JWT_SECRET"));
  const authController = new AuthController(authService, userService);

  const dataDeletionController = new DataDeletionController(appSecret);

  const trainingService = new TrainingService(trainerRepo, fletchlingRepo, openAI, query, phrases);
  const trainingController = new TrainingController(trainingService);


  ///// SETUP ROUTES:
  /// PUBLIC ZONE (no login required)
  subscriberController.setupRoutes(app);
  dataDeletionController.setup(app);
  mentionsController.setupRoutes(app);
  authController.setup(app);
  
  // Index
  app.get('/', function (req, res) { res.sendFile(path.resolve(basepath, 'index.html')) });

  // authorized zone
  authService.setupMiddleware(app);

  userController.setup(app);
  trainingController.setup(app);
  // const jwtSecret = getEnvVarOrThrow("JWT_SECRET");
  // configurePassport(userRepo, jwtSecret);
  // app.get('/protected', passport.authenticate('jwt', { session: false }), (req, res) => {
  //   // req.user is now set
  //   res.json({ message: 'This is a protected resource' });
  // });

  const server = http.createServer(app);
  await new Promise<void>(resolve => server.listen({ port: 4000 }, resolve));
}

start().catch(console.error).finally(() => console.log("done"));