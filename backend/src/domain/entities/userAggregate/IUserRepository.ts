import User from "./User";

export default interface IUserRepository {
  findByInstagramId(id: string): Promise<User | undefined>;
  findByTikTokId(id: string): Promise<User | undefined>;
  // findByWalletAddress(address: string): Promise<User | undefined>;
  getById(id: number): Promise<User>;
  save(user: User): Promise<void>;
  createNewUser(): Promise<User>;
}