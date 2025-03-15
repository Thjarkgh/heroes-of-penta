import axios from 'axios';
import IInstagramAuthAdapter, { ExchangeLongLivedTokenResult, ExchangeShortLivedTokenResult } from '../service/IInstagramAuthAdapter';
import ITikTokAuthAdapter, { ExchangeTokenResult } from '../service/ITikTokAuthAdapter';

interface TikTokCodeToTokenResponse {
  open_id: string; // The TikTok user's unique identifier.
  scope: string; // A comma-separated list (,) of the scopes the user has agreed to authorize.
  access_token: string; // The access token for future calls on behalf of the user.
  expires_in: number; // int64; The expiration of access_token in seconds. It is valid for 24 hours after initial issuance.
  refresh_token: string; // The token to refresh access_token. It is valid for 365 days after the initial issuance.
  refresh_expires_in: number; // int64; The expiration of refresh_token in seconds.
  token_type: string; // The value should be Bearer.
}

interface AxiosResponse<T> {
  // `data` is the response that was provided by the server
  data: T;

  // `status` is the HTTP status code from the server response
  status: number,

  // `statusText` is the HTTP status message from the server response
  // As of HTTP/2 status text is blank or unsupported.
  // (HTTP/2 RFC: https://www.rfc-editor.org/rfc/rfc7540#section-8.1.2.4)
  statusText: string,

  // `headers` the HTTP headers that the server responded with
  // All header names are lower cased and can be accessed using the bracket notation.
  // Example: `response.headers['content-type']`
  headers: any,

  // `config` is the config that was provided to `axios` for the request
  config: any,

  // `request` is the request that generated this response
  // It is the last ClientRequest instance in node.js (in redirects)
  // and an XMLHttpRequest instance in the browser
  request: any
};

export default class TikTokAuthAdapter implements ITikTokAuthAdapter {
  constructor(
    private readonly clientKey: string,
    private readonly clientSecret: string,
    private readonly redirectUri: string
  ) {}

  /**
   * Step 1: Exchange code for a short-lived token.
   */
  async exchangeToken(code: string): Promise<ExchangeTokenResult> {
    // const { code, clientId, clientSecret, redirectUri } = props;
    const url = 'https://open.tiktokapis.com/v2/oauth/token/';
    const data = {
      client_key: this.clientKey, // The unique identification key provisioned to the partner.
      client_secret: this.clientSecret, // The unique identification secret provisioned to the partner.
      code, // The authorization code from the web, iOS, Android or desktop authorization callback. The value should be URL decoded.
      grant_type: "authorization_code", // Its value should always be set as authorization_code.
      redirect_uri: this.redirectUri, // Its value must be the same as the redirect_uri used for requesting code.
      // code_verifier // Required for mobile and desktop app only. Code verifier is used to generate code challenge in PKCE authorization flow.
    };
    
    const response: AxiosResponse<TikTokCodeToTokenResponse> = await axios.post(url, data, { headers: { "Content-Type": "application/x-www-form-urlencoded" }});
    return {
      userId: response.data.open_id,
      accessToken: response.data.access_token,
      accessTokenExpiry: Date.now() + 1000 * response.data.expires_in,
      refreshToken: response.data.refresh_token,
      refreshTokenExpiry: Date.now() + 1000 * response.data.refresh_expires_in
    };
  }

  async refreshToken(refreshToken: string): Promise<ExchangeTokenResult> {
    // TODO
    throw new Error("not implemented")
  }
}
