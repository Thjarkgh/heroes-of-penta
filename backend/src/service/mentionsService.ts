import z from "zod";
import IMentionRepository from "./IMentionRepository";
import Mention from "../domain/entities/mentionAggregate/mention";

export const mentionData = z.string();
export type MentionData = typeof mentionData._type;

export default class MentionsService {
  constructor(
    private readonly repo: IMentionRepository
  ) {}

  async add(mention: MentionData) {
    const m = new Mention(mention, mention);
    await this.repo.push(m);
  } 
}