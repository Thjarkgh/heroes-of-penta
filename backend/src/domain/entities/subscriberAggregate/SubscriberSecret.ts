import { generateSecret } from "../../../helper/secret";

export default class SubscriberSecret {
  private constructor(
    public readonly validUntil: Date,
    public readonly secret: string
  ) {}

  public static async newSecret(purpose: string, validitySeconds: number) {
    const validUntil = new Date(Date.now() + validitySeconds * 1000);
    const secret = await generateSecret(purpose);
    return new SubscriberSecret(validUntil, secret.secret);
  }

  public static loadSecret(validUntil: Date, secret: string) {
    return new SubscriberSecret(validUntil, secret);
  }

  async validate(hash: string) {
    if (hash !== this.secret) return false;
    if (Date.now() > this.validUntil.valueOf()) return false;
    return true;
  }
}