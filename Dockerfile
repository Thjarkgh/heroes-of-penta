FROM node:20-slim AS base

RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y openssl

USER node

EXPOSE 4000

FROM node:20-slim AS build

RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y openssl
RUN npm i -g rimraf

RUN mkdir -p /home/node/app/node_modules && chown -R node:node /home/node/app
RUN mkdir -p /home/node/app/frontend
RUN mkdir -p /home/node/app/backend

USER node

WORKDIR /home/node/app

COPY ./package*.json ./
COPY ./backend/package*.json ./backend/
COPY ./frontend/package*.json ./frontend/

RUN npm ci

COPY --chown=node:node . .

RUN npm run build
RUN npm test

FROM base

USER node

RUN mkdir -p /home/node/app/build/node_modules && chown -R node:node /home/node/app
COPY --from=build /home/node/app/backend/package*.json /home/node/app/build/

WORKDIR /home/node/app/build

RUN npm ci --omit dev

COPY --from=build --chown=node:node /home/node/app/www /home/node/app/www
COPY --from=build --chown=node:node /home/node/app/build /home/node/app/build
COPY --from=build --chown=node:node /home/node/app/query.txt /home/node/app/query.txt
COPY --from=build --chown=node:node /home/node/app/dispositionMap.json /home/node/app/dispositionMap.json

ENTRYPOINT [ "node", "/home/node/app/build/index.js"  ]
