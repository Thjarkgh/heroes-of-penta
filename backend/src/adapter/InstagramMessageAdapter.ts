import axios, { RawAxiosRequestHeaders } from "axios";
import IInstagramMessageAdapter from "../service/IInstagramMessageAdapter";

export default class InstagramMessageAdapter implements IInstagramMessageAdapter {
  constructor(
    private readonly _userId: string,
    private readonly token: string
  ) {}

  userId() {
    return this._userId;
  }
  
  async respond(recipientId: string, message: string): Promise<void> {
    // const { code, clientId, clientSecret, redirectUri } = props;
    const url = `https://graph.instagram.com/v22.0/${this._userId}/messages`;
    const body = {
      recipient: {
          id: recipientId
      },
      message: {
          text: message
      }
    };
    const headers: RawAxiosRequestHeaders = {
      Authorization: `Bearer ${this.token}`,
      "Content-Type": "application/json"
    };

    const response = await axios.post(url, body, { headers });
  }
}