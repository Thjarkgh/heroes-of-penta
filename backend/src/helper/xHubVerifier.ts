import crypto from 'crypto';

export default class XHubVerifier {
  private readonly encoder: TextEncoder;

  constructor (
    private readonly secret: string
  ) {
    if (!secret) {
      throw new Error('Secret is required')
    }
    
    this.encoder = new TextEncoder()
  }

  sign(requestBody: string) {
    const hmac = crypto.createHmac("sha256", this.secret);
    hmac.update(requestBody, 'utf-8');
    return `sha256=${hmac.digest('hex')}`;
  }

  verify(expectedSignature: string, requestBody: string) {
    const expected = this.encoder.encode(expectedSignature);
    const actual = this.encoder.encode(this.sign(requestBody));
    if (expected.length !== actual.length) {
      return false;
    }
    
    return crypto.timingSafeEqual(expected, actual)
  }
}