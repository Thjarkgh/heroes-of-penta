const minBreakTime = 6 * 60 * 60 * 1000; // 6h

export default class Trainee {
  constructor(
    public readonly id: number,
    private _xp: number,
    private _disposition: Map<string, number>,
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

    let xp = 0;
    let max = 0;
    let min = 100;
    for (const d of disposition) {
      if (d[1] < 0 || d[1] > 100) {
        throw new Error(`Invalid disposition value ${d[1]} for ${d[0]}`);
      }

      if (d[1] > max) {
        max = d[1];
      }
      if (d[1] < min) {
        min = d[1];
      }
      const existing = this._disposition.get(d[0]);
      if (existing == undefined) {
        this._disposition.set(d[0], d[1]);
        xp += d[1];
      } else {
        this._disposition.set(d[0], existing + d[1]);
        xp += d[1];
      }
    }

    this._lastTraining = timestamp;
    const xpGain = Math.floor((xp / disposition.size) + 100 + max - min);
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