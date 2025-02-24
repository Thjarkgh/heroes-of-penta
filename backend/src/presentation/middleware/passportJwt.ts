// src/presentation/middlewares/passportJwt.ts
import passport from 'passport';
import { Strategy as JwtStrategy, ExtractJwt } from 'passport-jwt';
import IUserRepository from '../../domain/entities/userAggregate/IUserRepository';

export function configurePassport(userRepo: IUserRepository, jwtSecret: string) {
  passport.use(
    new JwtStrategy(
      {
        secretOrKey: jwtSecret,
        // Typically the token is in the Authorization header: "Bearer <token>"
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken()
      },
      async (payload, done) => {
        try {
          // payload.sub is our user ID
          const user = await userRepo.getById(payload.sub);
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
}
