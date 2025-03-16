import Trainee from "./Trainee";
import Trainer from "./Trainer";

export default interface ITrainerRepository {
  getOrCreateTrainer(userId: number): Promise<Trainer>;
  findTrainee(id: number): Promise<Trainee | undefined>;
  save(trainer: Trainer): Promise<void>;
}