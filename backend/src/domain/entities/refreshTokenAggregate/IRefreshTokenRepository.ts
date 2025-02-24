import RefreshToken from './RefreshToken';

export default interface IRefreshTokenRepository {
  create(refreshToken: RefreshToken): Promise<void>;
  findByToken(token: string): Promise<RefreshToken | null>;
  revokeToken(token: string): Promise<void>;
  removeToken(token: string): Promise<void>;
  // TODO: cleanupExpiredTokens() 
}
