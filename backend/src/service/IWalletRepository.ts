import Wallet from "../domain/entities/walletAggregate/Wallet";

export default interface IWalletRepository {
  getAccountWallet(userId: number): Promise<Wallet | null>;
}