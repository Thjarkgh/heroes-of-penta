export default interface ISmtpClient {
  sendConfirmationMail(address: string, text: string): Promise<void>;
}