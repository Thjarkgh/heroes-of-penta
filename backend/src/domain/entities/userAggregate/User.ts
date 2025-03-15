import InstagramAccount from "./InstagramAccount";
import TikTokAccount from "./TikTokAccount";
// import Wallet from "../walletAggregate/Wallet";

export default class User {
  constructor(
    public readonly id: number,
    // private _wallet: Wallet | null,
    private readonly _instagramAccounts: InstagramAccount[],
    private _tikTokAccount: TikTokAccount | null,
    // private _changeWalletTimeLock: number
  ) {}

  // get wallet() {
  //   return this._wallet;
  // }

  get instagramAccounts() {
    return new Array(...this._instagramAccounts);
  }

  get tikTokAccount() {
    return this._tikTokAccount;
  }

  // get changeWalletTimeLock() {
  //   return this._changeWalletTimeLock;
  // }

  // addWallet(wallet: Wallet) {
  //   if (Date.now() < this._changeWalletTimeLock) {
  //     throw new Error(`Cannot change wallet: Still on time-lock until ${(new Date(this._changeWalletTimeLock)).toISOString()}`);
  //   }
  //   const oldWallet = this._wallet;
  //   // one wallet is enough
  //   this._wallet = wallet;
  //   if (oldWallet?.user != null) {
  //     oldWallet.removeUser();
  //   }
  //   if (this._wallet.user?.id !== this.id) {
  //     this._wallet.assignUser(this);
  //   }

  //   // if (!!this._wallet.find((w) => w.address === wallet.address)) {
  //   //   throw new Error(`Already added ${wallet.address}`);
  //   // }
  //   // this._wallet.push(wallet);
  // }

  addInstagramAccount(instagramUserId: string, token: string, expiry: Date) {
    if (!!this._instagramAccounts.find((a) => a.id === instagramUserId)) {
      throw new Error(`Already added ${instagramUserId}`);
    }

    this._instagramAccounts.push(new InstagramAccount(instagramUserId, token, expiry));
  }

  // removeWallet(wallet: Wallet) {
  //   if (this._wallet?.address !== wallet.address) {
  //     throw new Error(`Cannot remove wallet ${wallet.address} - not assigned to this account!`);
  //   }
    
  //   this._wallet = null;
  //   if (wallet.user != null) {
  //     wallet.removeUser();
  //   }
  //   // const found = this._wallet.findIndex((w) => w.address === wallet.address);
  //   // if (found < 0) {
  //   //   throw new Error(`Wallet ${wallet.address} not found in current user`);
  //   // }

  //   // this._wallet.splice(found, 1);
  // }

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

  updateTikTokAccount(userId: string, accessToken: string, accessTokenExpiry: number, refreshToken: string, refreshTokenExpiry: number) {
    this._tikTokAccount = new TikTokAccount(userId, accessToken, accessTokenExpiry, refreshToken, refreshTokenExpiry);
  }

  updateTikTokAccountAccessToken(userId: string, token: string, expiry: number) {
    if (!this._tikTokAccount) {
      throw new Error(`Cannot update access token, if we have no TikTok account registered!`)
    }
    if (this._tikTokAccount.id !== userId) {
      throw new Error(`Cannot update access token, userId does not match registered TikTok account`);
    }

    this._tikTokAccount.updateAccessToken(token, expiry);
  }
}