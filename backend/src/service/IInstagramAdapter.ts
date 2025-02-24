export interface ExchangeShortLivedTokenResult {
  userId: string,
  accessToken: string
}

export interface ExchangeLongLivedTokenResult {
  token: string,
  expiry: Date
}

export default interface IInstagramAdapter {
  exchangeShortLivedToken(code: string): Promise<ExchangeShortLivedTokenResult>;
  exchangeLongLivedToken(shortLivedToken: string): Promise<ExchangeLongLivedTokenResult>;
}