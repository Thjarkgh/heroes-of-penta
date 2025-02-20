export default interface ISmtpClient {
  sendConfirmationMail(address: string, confirmCode: string, unsubscribeCode: string): Promise<void>;
  sendUnsubscribeMail(address: string, code: string): Promise<void>;
}