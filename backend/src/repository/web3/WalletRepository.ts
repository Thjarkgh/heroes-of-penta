import { ethers } from "ethers"
import Wallet from "../../domain/entities/walletAggregate/Wallet";

export default class WalletRepository {
  private readonly contract: ethers.Contract;

  constructor(
    rpcUrl: string,
    contractAddress: string
  ) {
    const provider = new ethers.JsonRpcProvider(rpcUrl)
    const abi = [
      // "function name() view returns (string)",
      // "function symbol() view returns (string)",
      "function getAccountCooldown(uint) view returns (uint)",
      "function getAccountWalletAddress(uint) view returns (address)",
      "function registerAccount(uint accountId)"

      // "event Transfer(address indexed from, address indexed to, uint amount)"
    ];

    // The Contract object
    this.contract = new ethers.Contract(contractAddress, abi, provider);
  }

  async getAccountWallet(userId: number): Promise<Wallet> {
    const address = await this.contract.getAccountWalletAddress(userId);
    return new Wallet(`${address}`);
  }
}