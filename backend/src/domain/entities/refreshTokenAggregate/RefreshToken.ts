export default class RefreshToken {
  public token: string;     // The random refresh token string
  public userId: number;    // The user who owns this token
  public expiresAt: Date;   // If you want expiry at DB level
  public revoked: boolean;  // For manual invalidation

  constructor(token: string, userId: number, expiresAt: Date, revoked: boolean = false) {
    this.token = token;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.revoked = revoked;
  }
}
