import Trainee from "./Trainee";
const minTrainerBreakTime = 60 * 1000; // 1 min for testing 6 * 60 * 60 * 1000; // 6h

export default class Trainer {
  constructor(
    public readonly id: number,
    private _maxTrainees: number,
    private _lastTraining: number,
    private _trainees: Trainee[]
  ) {
    if (this._trainees.length > this._maxTrainees) {
      throw new Error(`Too many trainees!`);
    }
  }

  setTrainees(trainees: Trainee[]) {
    if (trainees.length > this._maxTrainees) {
      throw new Error(`too many trainees!`);
    }
    this._trainees = trainees;
  }

  addTrainee(trainee: Trainee) {
    if (this._trainees.length < this._maxTrainees) {
      this._trainees.push(trainee);
    } else {
      throw new Error(`Already maxed out trainees`);
    }
  }

  get trainees() {
    return new Array(...this._trainees);
  }

  get lastTraining() {
    return this._lastTraining;
  }

  get maxTrainees() {
    return this._maxTrainees;
  }

  canTrain(timestamp: number = Date.now()) {
    return timestamp >= this._lastTraining + minTrainerBreakTime;
  }

  get traineeCount() {
    return this._trainees.length;
  }

  get readyTraineeCount() {
    return this._trainees.filter((t) => t.canTrainAgain).length;
  }

  train(timestamp: number, disposition: Map<string, number>) {
    let xpGain = 0;
    if (timestamp > Date.now()) {
      throw new Error(`Can't train in the future`);
    }
    if (!this.canTrain()) {
      throw new Error(`Still on break`);
    }

    for (const trainee of this.trainees) {
      xpGain += trainee.addDisposition(timestamp, disposition);
    }

    this._lastTraining = timestamp;
    return xpGain;
  }
}