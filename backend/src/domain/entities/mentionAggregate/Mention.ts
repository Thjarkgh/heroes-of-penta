export default class Mention {
  constructor(
    public readonly mediaId: string,
    public readonly commentId: string,
    public readonly timestamp: number
  ) {}
}