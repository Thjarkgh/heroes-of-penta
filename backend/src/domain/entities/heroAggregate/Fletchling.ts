export default class Fletchling {
  constructor(
    public readonly id: number,
    public name: string,
    public readonly description: string,
    public readonly imageUrl: string,
    public xp: number,
    // private _disposition: Map<string, number>
  ) {}

  // addDisposition(disposition: Map<string, number>) {
  //   let xp = 0;
  //   for (const d of disposition) {
  //     if (d[1] < 0 || d[1] > 100) {
  //       throw new Error(`Invalid disposition value ${d[1]} for ${d[0]}`);
  //     }

  //     const existing = this._disposition.get(d[0]);
  //     if (existing == undefined) {
  //       this._disposition.set(d[0], d[1]);
  //       xp += d[1];
  //     } else {
  //       this._disposition.set(d[0], existing + d[1]);
  //       xp += d[1];
  //     }
  //   }

  //   this._xp += 1; // TODO: should depend on disposition
  // }

  // get disposition() {
  //   return this._disposition;
  // }
}