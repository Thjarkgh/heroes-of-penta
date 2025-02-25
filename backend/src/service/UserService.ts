import IUserRepository from '../domain/entities/userAggregate/IUserRepository';
import IInstagramAuthAdapter from './IInstagramAuthAdapter';

export default class UserService {
  constructor(
    private readonly userRepo: IUserRepository,
    private readonly instagram: IInstagramAuthAdapter
  ) {}
  
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
}
