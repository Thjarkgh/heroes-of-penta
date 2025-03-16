export interface ExchangeTokenResult {
  userId: string,
  accessToken: string,
  accessTokenExpiry: number,
  refreshToken: string,
  refreshTokenExpiry: number
}

export default interface ITikTokAuthAdapter {
  exchangeToken(codeVerifier: string, code: string): Promise<ExchangeTokenResult>;
  refreshToken(refreshToken: string): Promise<ExchangeTokenResult>;
}