import Mention from "../domain/entities/mentionAggregate/mention";

export default interface IMentionRepository {
  push(m: Mention): Promise<void>;
}