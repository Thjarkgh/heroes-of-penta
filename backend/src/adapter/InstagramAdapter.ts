import axios from 'axios';
import IInstagramAdapter, { ExchangeLongLivedTokenResult, ExchangeShortLivedTokenResult } from '../service/IInstagramAdapter';

export default class InstagramAdapter implements IInstagramAdapter {
  constructor(
    private readonly appId: string,
    private readonly appSecret: string,
    private readonly redirectUri: string
  ) {}

  /**
   * Step 1: Exchange code for a short-lived token.
   */
  async exchangeShortLivedToken(code: string): Promise<ExchangeShortLivedTokenResult> {
    // const { code, clientId, clientSecret, redirectUri } = props;
    const url = 'https://api.instagram.com/oauth/access_token';
    const formData = new FormData();
    formData.append('client_id', this.appId);
    formData.append('client_secret', this.appSecret);
    formData.append('grant_type', 'authorization_code');
    formData.append('redirect_uri', this.redirectUri);
    formData.append('code', code);
    console.log(formData.toString());

    const response = await axios.post(url, formData, { headers: { "Content-Type": "multipart/form-data" }});
    console.log(JSON.stringify(response));
    // response.data => { access_token: ..., user_id: ... }
    return { accessToken: response.data.access_token, userId: response.data.user_id };
  }

  /**
   * Step 2: Exchange short-lived token for a long-lived token.
   */
  async exchangeLongLivedToken(shortLivedToken: string): Promise<ExchangeLongLivedTokenResult> {
    // const { shortLivedToken, clientSecret } = props;
    const url = 'https://graph.instagram.com/access_token';
    const resp = await axios.get(url, {
      params: {
        grant_type: 'ig_exchange_token',
        client_secret: this.appSecret,
        access_token: shortLivedToken
      }
    });
    console.log(JSON.stringify(resp));
    // resp.data => { access_token: 'LONG_LIVED_ACCESS_TOKEN', token_type: 'bearer', expires_in: 5184000 ... }
    return { token: resp.data.access_token, expiry: new Date(Date.now() + resp.data.expires_in * 1000) };
  }
}
