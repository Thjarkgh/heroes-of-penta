import { Express, Request, Response, NextFunction } from 'express';
import AuthService from '../../service/AuthService';
import UserService from '../../service/UserService';

export class AuthController {
  constructor(
    private readonly authService: AuthService,
    private readonly userService: UserService
  ) {}

  public setup(app: Express) {
    // We create a login route or use your existing Instagram callback.
    // This is a generic login route if we had email/password, etc.
    app.post('/auth/login/tiktok', this.loginTikTok.bind(this));

    // Refresh token route
    app.post('/auth/refresh', this.refresh.bind(this));

    // Logout route
    app.post('/auth/logout', this.logout.bind(this));
  }

  private async loginTikTok(req: Request, res: Response, next: NextFunction) {
    try {
      // Exchange TikTok code for Tokens
      let code = req.body.code as string;
      if (!code) {
        res.status(400).json({ error: 'Missing code' });
        return;
      }

      const userId = await this.userService.handleTikTokCallbackCode(code);

      const { accessToken, refreshToken } = await this.authService.generateTokens(userId);

      // Send tokens back; Access token can be in JSON, refresh in a cookie
      this.setRefreshTokenCookie(res, refreshToken);
      res.json({ accessToken });
    } catch (e: any) {
      res.status(500).json({ error: e.message });
    }
  }

  private async login(req: Request, res: Response, next: NextFunction) {
    try {
      // For Instagram or Metamask login, you'd have identified user ID by now.
      // We’ll assume you have userId from your logic. 
      // need to put tiktok
      const userId = req.body.userId;
      if (!userId) {
        res.status(401).json({ error: 'No userId' });
        return;
      }
      const { accessToken, refreshToken } = await this.authService.generateTokens(userId);

      // Send tokens back; Access token can be in JSON, refresh in a cookie
      this.setRefreshTokenCookie(res, refreshToken);
      res.json({ accessToken });
    } catch (e: any) {
      res.status(500).json({ error: e.message });
    }
  }

  private async refresh(req: Request, res: Response, next: NextFunction) {
    try {
      const oldRefreshToken = req.cookies['refresh_token'];
      if (!oldRefreshToken) {
        res.status(400).json({ error: 'Missing refresh token' });
        return;
      }
      // Rotate the token
      const { accessToken, refreshToken } = await this.authService.rotateRefreshToken(oldRefreshToken);
      this.setRefreshTokenCookie(res, refreshToken);
      res.json({ accessToken });
    } catch (error: any) {
      res.status(400).json({ error: 'Refresh token invalid or expired' });
    }
  }

  private async logout(req: Request, res: Response, next: NextFunction) {
    try {
      const refreshToken = req.cookies['refresh_token'];
      if (refreshToken) {
        // Revoke or remove
        await this.authService.removeToken(refreshToken);
      }
      // Clear cookie
      res.clearCookie('refresh_token', {
        httpOnly: true,
        secure: true,
        sameSite: 'strict'
      });
      res.json({ success: true });
    } catch (e: any) {
      res.status(500).json({ error: e.message });
    }
  }

  private setRefreshTokenCookie(res: Response, token: string) {
    // Set cookie with HttpOnly, secure, and sameSite
    res.cookie('refresh_token', token, {
      httpOnly: true,
      secure: true,       // true in production (requires HTTPS)
      sameSite: 'strict', // or 'lax' if needed
      path: '/auth/refresh', 
      maxAge: 1000 * 60 * 60 * 24 * 30 // 30 days in ms
    });
  }
}
