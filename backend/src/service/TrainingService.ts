import ITrainerRepository from "../domain/entities/heroAggregate/ITrainerRepository";
import Trainee from "../domain/entities/heroAggregate/Trainee";
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
    // TODO: Improve - no domain instantiations in service!
    trainer.setTrainees(heroIds.map((id) => trainer.trainees.find((t) => t.id === id) || new Trainee(id, 0, new Map(), 0)));
    const disposition = await this.openAiAdapter.analyzeImage(this.query, data);
    const xp = trainer.train(ts, new Map(Object.entries(disposition)));
    await this.trainingRepo.save(trainer);
    
    for (const heroId of heroIds) {
      const fletchling = await this.fletchlingRepo.getFletchling(heroId);
      fletchling.xp += xp;
      await this.fletchlingRepo.saveFletchling(fletchling);
    }
    return xp;
  }
}