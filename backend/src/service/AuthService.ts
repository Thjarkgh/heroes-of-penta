// src/application/AuthService.ts
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import IRefreshTokenRepository from '../domain/entities/refreshTokenAggregate/IRefreshTokenRepository';
import RefreshTokenEntity from '../domain/entities/refreshTokenAggregate/RefreshToken';
import passport from 'passport';
import { Strategy as JwtStrategy, ExtractJwt } from 'passport-jwt';
import IUserRepository from '../domain/entities/userAggregate/IUserRepository';
import { Express, Request, Response, NextFunction } from "express";

interface GenerateTokensResult {
  accessToken: string;
  refreshToken: string;
}

export default class AuthService {
  constructor(
    private readonly userRepo: IUserRepository,
    private readonly refreshRepo: IRefreshTokenRepository,
    private readonly jwtSecret: string
  ) {
  }


  setupMiddleware(app: Express) {
    app.use(passport.initialize());
    passport.serializeUser(function (user, done) {
      done(null, user);
    });
  
    passport.deserializeUser(function (user, done) {
      const err = null;
      done(err, user as Express.User);
    });

    passport.use(
      new JwtStrategy(
        {
          secretOrKey: this.jwtSecret,
          // Typically the token is in the Authorization header: "Bearer <token>"
          jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken()
        },
        async (payload, done) => {
          try {
            // payload.sub is our user ID
            const user = await this.userRepo.getById(payload.sub);
            if (!user) {
              return done(null, false);
            }
            return done(null, user); // attaches user to req.user
          } catch (err) {
            return done(err, false);
          }
        }
      )
    );
    app.use((req: Request, res: Response, next: NextFunction) => {
      /// for alternative cookie-auth
      // if ((req.headers.authorization || "") === "") {
      //     if ((req.headers.cookie?.length || 0) < 11) {
      //         return allowAnonymousAccess(req.originalUrl) ? next() : res.status(402).send("not logged in");
      //     }
      //     const cookies = req.headers.cookie?.split(";").map((p) => p.trim())
      //     const bearerCookie = cookies?.find((c) => !!c && c.startsWith('jwt=%22') && c.endsWith('%22'));
      //     if (!bearerCookie) {
      //         return allowAnonymousAccess(req.originalUrl) ? next() : res.status(402).send("not logged in");
      //     }
      //     req.headers.authorization = `Bearer ${bearerCookie.substring(7, bearerCookie.length - 3)}`;
      // }
      passport.authenticate('jwt', { session: false }, (err: any, user: any, info: any) => {
          if (user) {
              req.user = user;
              return next();
          }

          /// for unauthenticated frontend site calls => redirect to login
          // if ((req.headers.origin || req.headers.host) !== "") {
          //     return res.redirect(`${req.headers.origin || req.headers.host}/#!/login`);
          // }
          // res.redirect('/#!/login');

          return res.status(401).send("Not logged in");
          // next(new Error("Failed to authenticate"));
      })(req, res, next);
    });
  }
  /**
   * Create a short-lived JWT + a refresh token (persisted).
   */
  async generateTokens(userId: number): Promise<GenerateTokensResult> {
    // 1) Create Access Token
    const accessToken = jwt.sign(
      { sub: userId },
      this.jwtSecret,
      { expiresIn: '15m' } // short lifetime
    );

    // 2) Create a random Refresh Token
    const refreshTokenValue = uuidv4(); // Or crypto.randomBytes
    const expiresAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 30); // 30 days

    const refreshTokenEntity = new RefreshTokenEntity(
      refreshTokenValue,
      userId,
      expiresAt
    );
    await this.refreshRepo.create(refreshTokenEntity);

    return {
      accessToken,
      refreshToken: refreshTokenValue
    };
  }

  /**
   * Takes a refresh token, verifies it in DB, and rotates it (replaces with a new one).
   */
  async rotateRefreshToken(oldRefreshToken: string): Promise<GenerateTokensResult> {
    const tokenEntity = await this.refreshRepo.findByToken(oldRefreshToken);
    if (!tokenEntity || tokenEntity.revoked) {
      throw new Error('Invalid or revoked refresh token');
    }
    // Optional: check tokenEntity.expiresAt < now => also invalid

    // Revoke the old token or remove it
    await this.refreshRepo.removeToken(oldRefreshToken);

    // Now generate new tokens
    return this.generateTokens(tokenEntity.userId);
  }

  async removeToken(refreshToken: string): Promise<void> {
    await this.refreshRepo.removeToken(refreshToken);
  }
}
