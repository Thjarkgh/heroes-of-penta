// src/application/AuthService.ts
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import IRefreshTokenRepository from '../domain/entities/refreshTokenAggregate/IRefreshTokenRepository';
import RefreshTokenEntity from '../domain/entities/refreshTokenAggregate/RefreshToken';

interface GenerateTokensResult {
  accessToken: string;
  refreshToken: string;
}

export default class AuthService {
  constructor(
    private refreshRepo: IRefreshTokenRepository
  ) {}

  /**
   * Create a short-lived JWT + a refresh token (persisted).
   */
  async generateTokens(userId: number): Promise<GenerateTokensResult> {
    // 1) Create Access Token
    const accessToken = jwt.sign(
      { sub: userId },
      process.env.JWT_SECRET!,
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
