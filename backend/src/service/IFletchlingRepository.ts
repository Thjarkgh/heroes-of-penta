import Fletchling from "../domain/entities/heroAggregate/Fletchling";

export default interface IFletchlingRepository {
  getFletchlingsOfWallet(wallet: string): Promise<Fletchling[]>;
  getFletchling(id: number): Promise<Fletchling>;
}