import express from "express";
import cors from "cors";
import compression from "compression";
import http from "http";
import path from 'path';
import { existsSync, } from 'fs';

const getBaseDir = (dir: string) => {
  if (existsSync(path.resolve(dir, 'build')) && existsSync(path.resolve(dir, 'www'))) { return path.join('www'); }
  else if (existsSync(path.resolve(dir, '..', 'build')) && existsSync(path.resolve(dir, '..', 'www'))) { return path.join('..', 'www'); }
  else if (existsSync(path.resolve(dir, '..', '..', 'build')) && existsSync(path.resolve(dir, '..', '..', 'www'))) { return path.join('..', '..', 'www'); }
  else if (existsSync(path.resolve(dir, '..', '..', '..', 'build')) && existsSync(path.resolve(dir, '..', '..', '..', 'www'))) { return path.join('..', '..', '..', 'www'); }
  else {
    throw new Error(`Cannot find folder dist and build for Forntend to serve ${dir} and two folders up`);
  }
}

const start = async () => {
  const app = express();
  app.use(compression())
  app.use(cors());
  app.use(express.json({ limit: '1mb' }));  // for parsing application/json
  app.use(express.urlencoded({ extended: true, limit: '1mb' })) //for parsing application/x-www-form-urlencoded

  const basedir = getBaseDir(__dirname);
  const basepath = path.join(__dirname, basedir);
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

      // // just used to allow verification of ownership of a domain for Let's encrypt 
      // app.get('/.well-known',
      //   function (req, res) {
      //     console.log("test")
      //     const fileName = path.resolve(dir, basedir, 'certificates/cert')
      //     if (existsSync(fileName)) {
      //       res.sendFile(fileName) 
      //     } else {
      //       console.error("FILE NOT FOUND: " + fileName)
      //     }
      //   });
  
  app.get('/', function (req, res) { res.sendFile(path.resolve(basepath, 'index.html')) });

  const server = http.createServer(app);
  await new Promise<void>(resolve => server.listen({ port: 4000 }, resolve));
}

start().catch(console.error).finally(() => console.log("done"));