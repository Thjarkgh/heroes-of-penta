// src/presentation/controllers/UserController.ts
import { Express, Request, Response, NextFunction } from 'express';
import UserService from '../../service/UserService';
import User from '../../domain/entities/userAggregate/User';

export class UserController {
  constructor(
    private readonly userService: UserService, // injected
  ) {}

  public setup(app: Express) {
    // Callback route from Instagram: GET /connect/instagram
    //app.get('/connect/instagram', this.handleInstagramCallback.bind(this));
    // ...
    app.get('/user/me', this.me.bind(this));
    app.get('/user/nft-heroes', this.nftHeroes.bind(this));
    app.get('/user/nft-hero/:id', this.nftHeroe.bind(this));
  }

  private async nftHeroes(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user;
      if (!user) {
        res.status(401).send('Not logged in');
      } else {
        const result = await this.userService.getNftHeroesOfUser((user as User).id);
        res.json(result);
      }
    } catch (error) {
      res.status(500).send('Internal error fetching user');
    }
  }
  private async nftHeroe(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user;
      if (!user) {
        res.status(401).send('Not logged in');
      } else {
        const id = req.params.id;
        if (id == undefined) {
          res.status(500).send(`missing id`);
        } else {
          const result = await this.userService.getNft(Number.parseInt(id.toString()));
          res.json(result);
        }
      }
    } catch (error) {
      res.status(500).send('Internal error fetching user');
    }
  }

  private async me(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user;
      if (!user) {
        res.status(401).send('Not logged in');
      } else {
        const result = await this.userService.getUser((user as User).id);
        res.json(result);
      }
    } catch (error) {
      res.status(500).send('Internal error fetching user');
    }
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
