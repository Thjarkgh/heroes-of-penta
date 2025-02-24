import SubscriberSecret from "./SubscriberSecret";

const confirmationPurpose = "confirm";
const unsubscribePurpose = "unsubscribe";

export type SubscriberPurposeSecret = { purpose: string, secret: SubscriberSecret };

export default class Subscriber {
  private readonly secrets: Map<string, SubscriberSecret> = new Map();

  private constructor(
    public readonly email: string,
    private _isConfirmed: boolean
  ) {
  }

  public isConfirmed() {
    return this._isConfirmed;
  }

  public static createNew(email: string) {
    // TODO: validate email format
    return new Subscriber(email, false);
  }

  public static createExisting(email: string, isConfirmed: boolean, secrets: SubscriberPurposeSecret[]) {
    const subscriber = new Subscriber(email, isConfirmed);
    for (const secret of secrets) {
      if (subscriber.secrets.has(secret.purpose)) throw new Error(`Cannot have duplicate secrets when loading! Duplicate: ${secret.purpose}`);
      subscriber.secrets.set(secret.purpose, secret.secret);
    }
    return subscriber;
  }

  getSecrets(): SubscriberPurposeSecret[] {
    return new Array(...this.secrets.entries())
    .map((row) => ({ purpose: row[0], secret: row[1] }));
  }

  async createUnsubscribeCode() {
    const secret = await SubscriberSecret.newSecret(unsubscribePurpose, 60 * 60);
    this.secrets.set(unsubscribePurpose, secret);
    return secret.secret;
  }

  async isValidUnsubscribeCode(hash: string) {
    const secret = this.secrets.get(unsubscribePurpose);
    if (!secret) throw new Error('No unsubscribe secret');
    return secret.validate(hash);
  }

  async createConfirmationMailCode() {
    const secret = await SubscriberSecret.newSecret(confirmationPurpose, 60 * 60 * 24);
    // if there is already one, overwrite it
    this.secrets.set(confirmationPurpose, secret);
    return secret.secret;
  }

  async isValidEmailConfirmationCode(hash: string) {
    const secret = this.secrets.get(confirmationPurpose);
    if (!secret) return false;
    return secret.validate(hash);
  }

  async confirmEmail(hash: string) {
    const secret = this.secrets.get(confirmationPurpose);
    if (!secret) throw new Error(`No confirmation secret`);
    const isValid = secret.validate(hash);
    if (!isValid) throw new Error(`Invalid confirmation hash`);

    this.secrets.delete(confirmationPurpose);
    this._isConfirmed = true;
  }
}