import { Express } from "express";
import XHubVerifier from "../../helper/xHubVerifier";
import verifyXHubSignature from "../../helper/xHubMiddleware";
import MentionsService, { mentionData } from "../../service/MentionsService";

export default class InstagramMentionsController {
  private readonly verifier: XHubVerifier;

  constructor(
    private readonly service: MentionsService,
    private readonly appSecret: string,
    private readonly msgSecret: string
  ) {
    this.verifier = new XHubVerifier(appSecret);
  }

  setupRoutes(app: Express) {
    app.get("/webhooks/instagram", async (req, res, next) => {
      try {
        const mode = req.query["hub.mode"];
        const challenge = req.query["hub.challenge"];
        const secret = req.query["hub.verify_token"];

        if (mode !== "subscribe") {
          console.log(`invalid mode: ${mode}`);
          res.sendStatus(400);
          return;
        }
        if (secret !== this.msgSecret) {
          console.log(`invalid secret: ${secret}`);
          //throw new Error(`invalid secret`);
          res.sendStatus(400);
          return;
        }

        res.send(challenge);
      }
      catch (error) {
        next(error);
      }
    });

    app.post('/webhooks/instagram', async (req, res, next) => { console.log(JSON.stringify(req.body)); next(); }, verifyXHubSignature(this.appSecret),  async (req, res, next): Promise<void> => {
      try {
        const data = await mentionData.parseAsync(req.body, { async: true });
        await this.service.handle(data);
      }
      catch (err) {
        next(err);
      }
    });
  }
}