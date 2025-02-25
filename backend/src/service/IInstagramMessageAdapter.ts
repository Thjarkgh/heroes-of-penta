export default interface IInstagramMessageAdapter {
  userId(): string;
  respond(recipientId: string, message: string): Promise<void>;
}