import express from "express";
import cors from "cors";
import compression from "compression";
import http from "http";
import path from 'path';
import { getBaseDir } from "./helper/getBaseDir";
import { config } from "dotenv";
import SubscriberController from "./controller/subscriberController";
import SubscriberService from "./service/subscriberService";
import SmtpService from "./adapter/smtpService";
import SubscriberRepository from "./repository/mariadb/subscriberRepository";
import MentionsRepository from "./repository/mariadb/mentionsRepository";
import mariadb from "mariadb";
import MentionsService from "./service/mentionsService";
import MentionsController from "./controller/mentionsController";

const start = async () => {
  config();

  const app = express();
  app.use(compression())
  app.use(cors());
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
  const validateDefined = (x: string | undefined) => { if (!x) { throw new Error(`not defined`); } return x; };
  const smtpHost = validateDefined(process.env.SMTP_HOST);
  const smtpPort = Number.parseInt(validateDefined(process.env.SMTP_PORT));
  const smtpUser = validateDefined(process.env.SMTP_USER);
  const smtpPassword = validateDefined(process.env.SMTP_PASSWORD);
  const smtpSender = validateDefined(process.env.SMTP_FROM);
  const templatesFolder = path.join(__dirname, "..", "views");

  const database = validateDefined(process.env.MYSQL_DATABASE);
  if (database.includes('`')) throw new Error(`database must not include ${"`"}, invalid database name ${database}`);
  const pool = mariadb.createPool({
    host: validateDefined(process.env.MYSQL_HOST),
    user: validateDefined(process.env.MYSQL_USER),
    password: validateDefined(process.env.MYSQL_PASSWORD),
    database,
    port: Number.parseInt(validateDefined(process.env.MYSQL_PORT)),
    connectionLimit: 50
  });

  const smtpClient = new SmtpService(smtpHost, smtpPort, smtpUser, smtpPassword, smtpSender, templatesFolder);
  const subscriberRepository = await SubscriberRepository.createWithPool(pool, database);
  const subscriberService = new SubscriberService(subscriberRepository, smtpClient);
  const subscriberController = new SubscriberController(subscriberService);
  subscriberController.setupRoutes(app);
  
  app.get('/', function (req, res) { res.sendFile(path.resolve(basepath, 'index.html')) });

  const mentionsRepository = await MentionsRepository.createWithPool(pool, database);
  const mentionsService = new MentionsService(mentionsRepository);
  const appSecret = validateDefined(process.env.INSTAGRAM_APP_SECRET);
  const msgSecret = validateDefined(process.env.INSTAGRAM_MSG_SECRET);
  const mentionsController = new MentionsController(mentionsService, appSecret, msgSecret);
  mentionsController.setupRoutes(app);

  const server = http.createServer(app);
  await new Promise<void>(resolve => server.listen({ port: 4000 }, resolve));
}

start().catch(console.error).finally(() => console.log("done"));