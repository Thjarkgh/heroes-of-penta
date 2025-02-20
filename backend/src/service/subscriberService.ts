import ISmtpClient from "./ISmtpClient";
import ISubscriberRepository from "./ISubscriberRepository";

export default class SubscriberService {
  constructor(
    private readonly repository: ISubscriberRepository,
    private readonly smtpClient: ISmtpClient
  ) {}
  
  async subscribe(email: string) {
    const regex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/g;
    if (!regex.test(email)) {
      return { status: "not_an_email" } as const;
    }
    const has = await this.repository.has(email);
    if (has) {
      const subscriber = await this.repository.get(email);
      if (subscriber.isConfirmed())
        return { status: "already_registered_and_confirmed" } as const;
      else
        return { status: "already_registered" } as const;
    }
    const subscriber = await this.repository.create(email);
    const confirmationRequest = await subscriber.createConfirmationMailCode();
    const unsubscribeCode = await subscriber.createUnsubscribeCode();
    await this.smtpClient.sendConfirmationMail(email, confirmationRequest, unsubscribeCode);
    await this.repository.save(subscriber);
    return { status: "ok" as const, email };
  }

  async requestNewConfirmationMail(email: string) {
    const subscriber = await this.repository.get(email);
    const confirmationRequest = await subscriber.createConfirmationMailCode();
    const unsubscribeCode = await subscriber.createUnsubscribeCode();
    await this.repository.save(subscriber);
    await this.smtpClient.sendConfirmationMail(email, confirmationRequest, unsubscribeCode);
  }

  async confirmMail(email: string, hash: string) {
    const subscriber = await this.repository.get(email);
    if (await subscriber.isValidEmailConfirmationCode(hash)) {
      await subscriber.confirmEmail(hash);
      await this.repository.save(subscriber);
      return true;
    } else {
      return false;
    }
  }

  async requestUnsubscribeCode(email: string) {
    const subscriber = await this.repository.get(email);
    const code = await subscriber.createUnsubscribeCode();
    await this.repository.save(subscriber);
    return code;
  }

  async confirmUnsubscribe(email: string, hash: string) {
    const subscriber = await this.repository.get(email);
    if (await subscriber.isValidUnsubscribeCode(hash)) {
      await this.repository.delete(subscriber);
      return true;
    } else {
      return false;
    }
  }
}