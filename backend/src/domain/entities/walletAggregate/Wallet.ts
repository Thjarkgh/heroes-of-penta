import User from "../userAggregate/User";

export default class Wallet {
  constructor(
    public readonly address: string,
    // private _user: User | null,
    // private _changeTimeLockUntil: number
  ) {}

  // get user() {
  //   return this._user;
  // }

  // get changeTimeLockUntil() {
  //   return this._changeTimeLockUntil;
  // }

  // assignUser(user: User) {
  //   if (Date.now() < this._changeTimeLockUntil) {
  //     throw new Error(`Wallet is still on time-lock until ${(new Date(this._changeTimeLockUntil)).toISOString()}`);
  //   }
  //   this._user = user;
  //   this._changeTimeLockUntil = Date.now() + 7 * 24 * 60 * 60 * 1000;
  //   if (user.wallet?.address !== this.address) {
  //     user.addWallet(this);
  //   }
  // }

  // removeUser() {
  //   const u = this._user;
  //   this._user = null;
  //   if (u?.wallet?.address === this.address) {
  //     u.removeWallet(this);
  //   }
  // }
}