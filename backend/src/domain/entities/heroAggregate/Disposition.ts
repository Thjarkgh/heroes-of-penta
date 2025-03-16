export default class Disposition {
  constructor(
    private _disposition: Map<string, number>
  ) {}

  addDisposition(disposition: Map<string, number>) {
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

    const xpGain = Math.floor((xp / disposition.size) + 100 + max - min);
    return xpGain;
  }

  get disposition() {
    return new Map(this._disposition);
  }
}