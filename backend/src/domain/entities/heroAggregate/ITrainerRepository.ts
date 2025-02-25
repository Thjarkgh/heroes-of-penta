import Trainer from "./Trainer";

export default interface ITrainerRepository {
  getOrCreateTrainer(userId: number): Promise<Trainer>;
  save(trainer: Trainer): Promise<void>;
}