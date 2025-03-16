import Disposition from "./Disposition";
import Trainee from "./Trainee";
const minTrainerBreakTime = 60 * 1000; // 1 min for testing 6 * 60 * 60 * 1000; // 6h

export default class Trainer {
  constructor(
    public readonly id: number,
    private _maxTrainees: number,
    private _lastTraining: number,
    private _trainees: Trainee[],
    private _trainerXP: number,
    private _leftoverXP: number,
    private readonly _disposition: Disposition
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

  addNewTrainee(id: number) {
    if (this._trainees.length >= this._maxTrainees) {
      throw new Error(`Already maxed out trainees`);
    }

    const dispo = new Disposition(this._disposition.disposition)
    const trainee = new Trainee(id, this._leftoverXP, dispo, 0);
    this._leftoverXP = 0;
    this._trainees.push(trainee);
    return trainee;
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

  get trainerXP() {
    return this._trainerXP;
  }

  get leftoverXP() {
    return this._leftoverXP;
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

  get disposition() {
    return this._disposition;
  }

  train(timestamp: number, disposition: Map<string, number>) {
    let xpGain = 0;
    if (timestamp > Date.now()) {
      throw new Error(`Can't train in the future`);
    }
    if (!this.canTrain()) {
      throw new Error(`Still on break`);
    }

    if (this._trainees.length === 0) {
      xpGain += this._disposition.addDisposition(disposition);
      this._leftoverXP += xpGain;
    } else {
      for (const trainee of this.trainees) {
        xpGain += trainee.addDisposition(timestamp, disposition);
      }
      xpGain = xpGain / this._trainees.length;
    }

    this._lastTraining = timestamp;
    this._trainerXP += xpGain;
    return xpGain;
  }
}