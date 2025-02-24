import InstagramAccount from "./InstagramAccount";
import Wallet from "./Wallet";

export default class User {
  constructor(
    public readonly id: number,
    private readonly _wallets: Wallet[],
    private readonly _instagramAccounts: InstagramAccount[]
  ) {}

  get wallets() {
    return new Array(...this._wallets);
  }

  get instagramAccounts() {
    return new Array(...this._instagramAccounts);
  }

  addWallet(wallet: Wallet) {
    if (!!this._wallets.find((w) => w.address === wallet.address)) {
      throw new Error(`Already added ${wallet.address}`);
    }

    this._wallets.push(wallet);
  }

  addInstagramAccount(instagramUserId: string, token: string, expiry: Date) {
    if (!!this._instagramAccounts.find((a) => a.id === instagramUserId)) {
      throw new Error(`Already added ${instagramUserId}`);
    }

    this._instagramAccounts.push(new InstagramAccount(instagramUserId, token, expiry));
  }

  removeWallet(wallet: Wallet) {
    const found = this._wallets.findIndex((w) => w.address === wallet.address);
    if (found < 0) {
      throw new Error(`Wallet ${wallet.address} not found in current user`);
    }

    this._wallets.splice(found, 1);
  }

  removeInstagramAccount(account: InstagramAccount) {
    const found = this._instagramAccounts.findIndex((a) => a.id === account.id);
    if (found < 0) {
      throw new Error(`Instagram Account ${account.id} not found in current user`);
    }
    this._instagramAccounts.splice(found, 1);
  }

  updateInstagramAccountToken(instagramUserId: string, token: string, expiry: Date) {
    const account = this._instagramAccounts.find((a) => a.id === instagramUserId);
    if (!account) {
      throw new Error(`Current user has no instagram account with id ${instagramUserId}`);
    }
    account.updateToken(token, expiry);

  }
}