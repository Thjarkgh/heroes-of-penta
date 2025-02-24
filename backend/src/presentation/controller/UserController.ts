// src/presentation/controllers/UserController.ts
import { Express, Request, Response, NextFunction } from 'express';
import UserService from '../../service/UserService';

export class UserController {
  constructor(
    private readonly userService: UserService, // injected
  ) {}

  public setup(app: Express) {
    // Callback route from Instagram: GET /connect/instagram
    app.get('/connect/instagram', this.handleInstagramCallback.bind(this));
    // ...
  }

  private async handleInstagramCallback(req: Request, res: Response, next: NextFunction) {
    try {
      // Example: /connect/instagram?code=abcdefg#_
      let code = req.query.code as string;
      if (!code) {
        res.status(400).send('Missing code');
        return;
      }

      await this.userService.handleInstagramCallbackCode(code);

      // Then redirect the user to the "Account" page on the frontend
      // Possibly with a session token or query param to indicate success
      res.redirect('https://heroesofpenta.com/account?newUser=true');
    } catch (error: any) {
      console.error('Error exchanging code for token:', error.message);
      res.status(500).send('Error exchanging code for token');
    }
  }
}
