export default class TikTokAccount {
  constructor(
    public readonly id: string,
    private _accessToken: string,
    private _accessTokenExpiry: number,
    private _refreshToken: string,
    private _refreshTokenExpiry: number
  ) {}

  get accessToken() {
    return this._accessToken;
  }

  get accessTokenExpiry() {
    return this._accessTokenExpiry;
  }

  get refreshToken() {
    return this._refreshToken;
  }

  get refreshTokenExpiry() {
    return this._refreshTokenExpiry;
  }

  sameAs(other: TikTokAccount): boolean {
    return this.id === other.id
      && this.accessToken === other.accessToken && this.accessTokenExpiry === other.accessTokenExpiry
      && this.refreshToken === other.refreshToken && this.refreshTokenExpiry === other.refreshTokenExpiry;
  }

  updateAccessToken(token: string, expiry: number) {
    this._accessToken = token;
    this._accessTokenExpiry = expiry;
  }

  updateRefreshToken(token: string, expiry: number) {
    this._refreshToken = token;
    this._refreshTokenExpiry = expiry;
  }
}