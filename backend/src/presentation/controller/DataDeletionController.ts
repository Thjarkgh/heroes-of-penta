import { Request, Response, Express } from 'express';
import { createHmac } from 'crypto';

export class DataDeletionController {
  private readonly appSecret: string;

  /**
   * Pass the Facebook App Secret in the constructor.
   */
  constructor(appSecret: string) {
    this.appSecret = appSecret;
  }

  /**
   * Set up the controller routes on the given Express application.
   */
  public setup(app: Express): void {
    app.post('/data_deletion', (req: Request, res: Response) => {
      // Extract the signed_request from the request body
      const { signed_request } = req.body;

      if (!signed_request) {
        res.status(400).json({ error: 'Missing signed_request in body.' });
        return;
      }

      // Parse and validate the signed_request
      const data = this.parseSignedRequest(signed_request);

      if (!data) {
        res.status(400).json({ error: 'Invalid signed_request.' });
        return;
      }

      // You can retrieve the user_id if needed
      const userId = data.user_id;
      console.log(`User ID to delete data for: ${userId}`);

      // TODO: Delete data (so far we have nothing to delete, but we will in the future)

      // Prepare response for data deletion callback
      const statusUrl = 'https://www.heroesofpenta.com/deletion?id=abc123';
      const confirmationCode = 'abc123';

      res.json({
        url: statusUrl,
        confirmation_code: confirmationCode,
      });
    });
  }

  /**
   * Parse the signed_request payload using your app secret.
   * Returns the decoded data or null if the signature is invalid.
   */
  private parseSignedRequest(signedRequest: string): any {
    // signed_request is made up of two parts: {encodedSig}.{payload}
    const [encodedSig, payload] = signedRequest.split('.', 2);

    if (!encodedSig || !payload) {
      return null;
    }

    // Decode the signature
    const sigBuffer = this.base64UrlDecode(encodedSig);
    // Decode and parse the data
    const dataString = this.base64UrlDecode(payload).toString('utf-8');
    const data = JSON.parse(dataString);

    // Verify the signature
    const expectedSig = createHmac('sha256', this.appSecret)
      .update(payload)
      .digest();

    // Compare the computed HMAC with the provided signature
    if (!this.bufferEqual(sigBuffer, expectedSig)) {
      console.error('Bad Signed JSON signature!');
      return null;
    }

    return data;
  }

  /**
   * Helper to Base64 URL-decode a string.
   */
  private base64UrlDecode(input: string): Buffer {
    // Replace -_ back to +/
    const base64String = input.replace(/-/g, '+').replace(/_/g, '/');
    return Buffer.from(base64String, 'base64');
  }

  /**
   * A small utility to compare two Buffers in a timing-safe way.
   */
  private bufferEqual(a: Buffer, b: Buffer): boolean {
    if (a.length !== b.length) {
      return false;
    }
    let result = 0;
    for (let i = 0; i < a.length; i++) {
      result |= a[i] ^ b[i];
    }
    return result === 0;
  }
}
