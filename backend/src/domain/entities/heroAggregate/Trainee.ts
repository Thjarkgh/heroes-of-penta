import Disposition from "./Disposition";

const minBreakTime = 60 * 1000; // 1 min for testing 6 * 60 * 60 * 1000; // 6h

export default class Trainee {
  constructor(
    public readonly id: number,
    private _xp: number,
    private _disposition: Disposition,
    private _lastTraining: number
  ) {}

  get canTrainAgain() {
    return Date.now() > minBreakTime + this._lastTraining;
  }

  addDisposition(timestamp: number, disposition: Map<string, number>) {
    if (!this.canTrainAgain) {
      throw new Error(`Still on break time!`);
    }
    if (timestamp > Date.now()) {
      throw new Error(`Can't register future training!`);
    }

    const xpGain = this._disposition.addDisposition(disposition);

    this._lastTraining = timestamp;
    this._xp += xpGain;
    return xpGain;
  }

  get disposition() {
    return this._disposition;
  }

  get xp() {
    return this._xp;
  }

  get lastTraining() {
    return this._lastTraining;
  }
}