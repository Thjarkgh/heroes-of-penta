import express, { Express } from "express";
import SubscriberService from "../service/subscriberService";

export default class SubscriberController {
  constructor(
    private readonly service: SubscriberService 
  ) {}

  setupRoutes(app: Express ) {
    app.set("view engine", "ejs"); // Set up EJS for templating
    app.post('/subscribe', async (req, res, next): Promise<void> => {
      try {
        const email = req.body.email;
        if (!email) {
          res.status(400).send("No e-mail address provided");
          return;
        }
        const result = await this.service.subscribe(req.body.email);
        switch (result.status) {
          case "already_registered_and_confirmed": {
            res.status(409).send("This e-mail address is already registered");
            break;
          }
          case "already_registered": {
            res.status(200).send(`/incompleteregistration?email=${email}`);
            break;
          }
          case "not_an_email": {
            res.status(400).send("The provided e-mail address is not a valid e-mail address");
            break;
          }
          case "ok": {
            res.status(201).send("ok");
            break;
          }
          default: {
            res.status(500).send(`Internal component return unknown status ${(result as any).status}`);
          }
        }
      }
      catch (err) {
        next(err);
      }
    });

    // routes not called from frontend
    app.get("/incompleteregistration", async (req, res, next) => res.render("requestNewConfirmationMail", { email: req.query.email }))
    app.post("/requestnewconfirmationmail", this.requestNewConfirmationMail.bind(this))
    app.get('/unsubscribe', this.unsubscribe.bind(this));
    app.post("/unsubscribe/confirm", this.confirmUnsubscribe.bind(this));
    app.get('/confirm', this.confirm.bind(this));
  }

  async requestNewConfirmationMail(req: express.Request, res: express.Response, next: express.NextFunction) {
    try {
      const email = req.body.email;
      if (!email) return void(res.status(400).send("Email is required"));
      if (Array.isArray(email)) return void(res.status(400).send("Only one email!"));
      const result = await this.service.requestNewConfirmationMail(email.toString());
      res.render("sentNewConfirmationMail");
    }
    catch (err) {
      next(err);
    }
  }
  
  async unsubscribe(req: express.Request, res: express.Response, next: express.NextFunction): Promise<void> {
    try {
      const email = req.query.email;
      if (!email) return void(res.status(400).send("Email is required"));
      if (Array.isArray(email)) return void(res.status(400).send("Only one email!"));
  
      const unsubscribeSecret = await this.service.requestUnsubscribeCode(email.toString());
  
      return res.render("unsubscribe", { email, unsubscribeSecret });
    }
    catch(err) {
      next(err);
    }
  }

  async confirmUnsubscribe(req: express.Request, res: express.Response, next: express.NextFunction): Promise<void> {
    try {
      const { email, secret } = req.body;
      if (!email || !secret) return void(res.status(400).send("Missing parameters"));
  
      const success = await this.service.confirmUnsubscribe(email, secret);
      
      if (success) {
          return res.render("unsubscribed");
      } else {
          return void(res.status(400).send("Invalid unsubscribe request."));
      }
    }
    catch (err) {
      next(err);
    }
}

  async confirm(req: express.Request, res: express.Response, next: express.NextFunction): Promise<void> {
    try {
      const email = req.query.email;
      if (!email) return void(res.status(400).send("Email is required"));
      if (Array.isArray(email)) return void(res.status(400).send("Only one email!"));
      const token = req.query.token;
      if (!token) return void(res.status(400).send("No secret"));
      if (Array.isArray(token)) return void(res.status(400).send("Only one secret!"));

      const result = await this.service.confirmMail(email.toString(), token.toString());

      if (result) {
        return res.render("subscribed", {});
      } else {
        res.status(400).send("Invalid confirmation");
      }
    }
    catch (err) {
      next(err);
    }
  }
}