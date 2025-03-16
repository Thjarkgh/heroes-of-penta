import ITrainerRepository from "../domain/entities/heroAggregate/ITrainerRepository";
import IFletchlingRepository from "./IFletchlingRepository";
import IOpenAiAdapter from "./IOpenAiAdapter";

export default class TrainingService {
  constructor(
    private readonly trainingRepo: ITrainerRepository,
    private readonly fletchlingRepo: IFletchlingRepository,
    private readonly openAiAdapter: IOpenAiAdapter,
    private readonly query: string,
    private readonly dispoPhrases: {[key: string]: string}
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
    console.log(JSON.stringify(disposition));
    const dispositions = Object.entries(disposition);
    if (dispositions.length < 1) {
      throw new Error(`empty dispositions!`);
    }
    if (dispositions.length != 25) {
      console.log(`Weird, only got ${dispositions.length} dispositions: ${JSON.stringify(disposition)}`);
    }
    let highestDisposition: [string, number] = dispositions[0];
    for (const dispo of dispositions) {
      if (dispo[1] > highestDisposition[1]) {
        highestDisposition = dispo;
      }
    }
    const phrase = this.dispoPhrases[highestDisposition[0]] || "You nailed it!";
    if (phrase === "You nailed it!") {
      console.log(`Highest scoring disposition not found in phrases: ${highestDisposition[0]}`);
    }

    const xp = trainer.train(ts, new Map(dispositions));
    console.log(`Training yielded ${xp}`);
    await this.trainingRepo.save(trainer);
    
    console.log(`Save ${trainer.trainees.length} trainees (${trainer.traineeCount})`);
    for (const trainee of trainer.trainees) {
      const fletchling = await this.fletchlingRepo.getFletchling(trainee.id);
      if (fletchling.xp !== trainee.xp) {
        console.log(`update fletchling ${fletchling.id}`);
        fletchling.xp = trainee.xp;
        await this.fletchlingRepo.saveFletchling(fletchling);
      }
    }
    return { xp, phrase };
  }
}