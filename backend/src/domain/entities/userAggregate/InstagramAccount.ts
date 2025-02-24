export default class InstagramAccount {
  constructor(
    public readonly id: string,
    private _longLivedToken: string | undefined,
    private _expiry: Date | undefined
  ) {}

  get longLivedToken() {
    return this._longLivedToken;
  }

  get expiry() {
    return this._expiry;
  }

  updateToken(token: string, expiry: Date) {
    this._longLivedToken = token;
    this._expiry = expiry;
  }
}