import nodemailer from "nodemailer";
import ISmtpClient from "../service/ISmtpClient";
import ejs from "ejs";
import path from "path";
import { promisify } from "util";

export default class SmtpService implements ISmtpClient {
  constructor(
    private readonly host: string,
    private readonly port: number,
    private readonly user: string,
    private readonly password: string,
    private readonly sender: string,
    private readonly templatesFolder: string
  ) {}

  async sendConfirmationMail(email: string, secret: string): Promise<void> {
    const transport = nodemailer.createTransport({
      host: this.host,
      port: this.port,
      secure: this.port === 465,
      auth: {
        user: this.user,
        pass: this.password
      }
    });
    const mail = await ejs.renderFile(path.join(this.templatesFolder, "requestConfirmationMail.ejs"), { email, secret });
    const result = await transport.sendMail({
      to: email,
      from: this.sender,
      subject: "Please confirm your e-mail address!",
      html: mail,
      text: `Welcome to the Heroes of Penta!

Please confirm your e-mail address by opening the following link: https://heroesofpenta.com/confirm?email=${email}&token=${secret}

If you instead wish to unsubscribe, you can do so at any time by opening the following link: https://heroesofpenta.com/unsubscribe?email=${email}`
    });
    if (result.rejected.length !== 0 || result.accepted.length !== 1) {
      throw new Error(`SMTP server did not accept email!`);
    }
  }
}