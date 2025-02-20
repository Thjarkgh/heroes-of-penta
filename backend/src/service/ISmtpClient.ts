export default interface ISmtpClient {
  sendConfirmationMail(address: string, confirmCode: string, unsubscribeCode: string): Promise<void>;
}