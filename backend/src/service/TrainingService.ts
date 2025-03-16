import ITrainerRepository from "../domain/entities/heroAggregate/ITrainerRepository";
import IFletchlingRepository from "./IFletchlingRepository";
import IOpenAiAdapter from "./IOpenAiAdapter";

export default class TrainingService {
  constructor(
    private readonly trainingRepo: ITrainerRepository,
    private readonly fletchlingRepo: IFletchlingRepository,
    private readonly openAiAdapter: IOpenAiAdapter,
    private readonly query: string
  ) {}

  async train(userId: number, heroIds: number[], data: Buffer) {
    const ts = Date.now();
    const trainer = await this.trainingRepo.getOrCreateTrainer(userId);
    // TODO: check timeout for training
    if (!trainer.canTrain()) {
      throw new Error(`Wait some more!`);
    }
    const trainees = trainer.trainees.filter((t) => !!heroIds.find((h) => h === t.id));
    // TODO: Imptove => add remove method
    trainer.setTrainees(trainees);
    const newTraineeIds = heroIds.filter((h) => !trainer.trainees.find((t) => t.id === h));
    for (const newTraineeId of newTraineeIds) {
      const newTrainee = await this.trainingRepo.findTrainee(newTraineeId);
      if (!!newTrainee) {
        trainer.addTrainee(newTrainee);
      } else {
        trainer.addNewTrainee(newTraineeId);
      }
    }
    const disposition = await this.openAiAdapter.analyzeImage(this.query, data);
    const xp = trainer.train(ts, new Map(Object.entries(disposition)));
    await this.trainingRepo.save(trainer);
    
    for (const trainee of trainer.trainees) {
      const fletchling = await this.fletchlingRepo.getFletchling(trainee.id);
      if (fletchling.xp !== trainee.xp) {
        fletchling.xp = trainee.xp;
        await this.fletchlingRepo.saveFletchling(fletchling);
      }
    }
    return xp;
  }
}