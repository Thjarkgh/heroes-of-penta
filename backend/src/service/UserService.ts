import IUserRepository from '../domain/entities/userAggregate/IUserRepository';
import IFletchlingRepository from './IFletchlingRepository';
import IInstagramAuthAdapter from './IInstagramAuthAdapter';
import ITikTokAuthAdapter from './ITikTokAuthAdapter';
import IWalletRepository from './IWalletRepository';

export interface ApiUser {
  id: number;
  name: string;
  metamaskAddress: string | null;
  maxTrainees: number;
}

export interface ApiFletchling {
  id: number,
  name: string,
  description: string,
  imageUrl: string,
  xp: number
}

export default class UserService {
  constructor(
    private readonly userRepo: IUserRepository,
    private readonly instagram: IInstagramAuthAdapter,
    private readonly tiktok: ITikTokAuthAdapter,
    private readonly walletRepo: IWalletRepository,
    private readonly fletchlingRepo: IFletchlingRepository
  ) {}
  
  async getUser(userId: number): Promise<ApiUser> {
    const user = await this.userRepo.getById(userId);
    let name: string | undefined = user.tikTokAccount?.id;
    if (name == undefined) {
      name = user.instagramAccounts.reduce((prev: string | undefined, cur, idx, arr) => prev || cur.id, name);
    }
    const wallet = await this.walletRepo.getAccountWallet(userId);
    return {
      id: user.id,
      name: name || "?",
      metamaskAddress: wallet?.address || null,
      maxTrainees: 1 // TODO: for way later
    };
  }

  async getNftHeroesOfUser(userId: number): Promise<ApiFletchling[]> {
    const wallet = await this.walletRepo.getAccountWallet(userId);
    if (!wallet) {
      throw new Error(`User ${userId} has no wallet`);
    }
    const fletchlings = await this.fletchlingRepo.getFletchlingsOfWallet(wallet.address);
    return fletchlings.map((f) => ({
      id: f.id,
      name: f.name,
      description: f.description,
      imageUrl: f.imageUrl,
      xp: f.xp
    }));
  }

  async getNft(nftId: number): Promise<ApiFletchling> {
    const fletchling = await this.fletchlingRepo.getFletchling(nftId);
    return fletchling;
  }
  
  async handleInstagramCallbackCode(code: string) {
    // Example: /connect/instagram?code=abcdefg#_
    if (!code) {
      throw new Error(`invalid code ${code}`);
    }

    // Possibly strip #_ if it appears
    code = code.replace('#_', '');

    const shortLivedTokenResult = await this.instagram.exchangeShortLivedToken(code);
    console.log(JSON.stringify(shortLivedTokenResult));

    const longLivedTokenResult = await this.instagram.exchangeLongLivedToken(shortLivedTokenResult.accessToken);
    console.log(JSON.stringify(longLivedTokenResult));

    // Now decide if we are "registering" or "logging in".
    const existing = await this.userRepo.findByInstagramId(shortLivedTokenResult.userId);
    if (!!existing) {
      existing.updateInstagramAccountToken(shortLivedTokenResult.userId, longLivedTokenResult.token, longLivedTokenResult.expiry);
      await this.userRepo.save(existing);
    } else {
      const user = await this.userRepo.createNewUser();
      user.addInstagramAccount(shortLivedTokenResult.userId, longLivedTokenResult.token, longLivedTokenResult.expiry);
      await this.userRepo.save(user);
    }
  }

  async handleTikTokCallbackCode(code: string) {
    // Example: /connect/instagram?code=abcdefg#_
    if (!code) {
      throw new Error(`invalid code ${code}`);
    }

    const tokenResult = await this.tiktok.exchangeToken(code);
    console.log(JSON.stringify(tokenResult));

    // Now decide if we are "registering" or "logging in with token refresh".
    const existing = await this.userRepo.findByTikTokId(tokenResult.userId);
    if (!!existing) {
      existing.updateTikTokAccount(tokenResult.userId, tokenResult.accessToken, tokenResult.accessTokenExpiry, tokenResult.refreshToken, tokenResult.refreshTokenExpiry);
      await this.userRepo.save(existing);
      return existing.id;
    } else {
      const user = await this.userRepo.createNewUser();
      user.updateTikTokAccount(tokenResult.userId, tokenResult.accessToken, tokenResult.accessTokenExpiry, tokenResult.refreshToken, tokenResult.refreshTokenExpiry);
      await this.userRepo.save(user);
      return user.id;
    }
  }

  async refreshTikTokToken(userId: number) {
    const user = await this.userRepo.getById(userId);
    const tikTokAccount = user.tikTokAccount;

    if (tikTokAccount == null) {
      throw new Error(`Cannot refresh TikTok access token of a user without TikTok account!`);
    }

    const result = await this.tiktok.refreshToken(tikTokAccount.refreshToken);

    user.updateTikTokAccount(result.userId, result.accessToken, result.accessTokenExpiry, result.refreshToken, result.refreshTokenExpiry);
    await this.userRepo.save(user);
  }
}
