import IUserRepository from '../domain/entities/userAggregate/IUserRepository';
import IInstagramAdapter from './IInstagramAdapter';

export default class UserService {
  constructor(
    private readonly userRepo: IUserRepository,
    private readonly instagram: IInstagramAdapter
  ) {}
  
  async handleInstagramCallbackCode(code: string) {
    // Example: /connect/instagram?code=abcdefg#_
    if (!code) {
      throw new Error(`invalid code ${code}`);
    }

    // Possibly strip #_ if it appears
    code = code.replace('#_', '');

    const shortLivedTokenResult = await this.instagram.exchangeShortLivedToken(code);

    const longLivedTokenResult = await this.instagram.exchangeLongLivedToken(shortLivedTokenResult.accessToken);

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
