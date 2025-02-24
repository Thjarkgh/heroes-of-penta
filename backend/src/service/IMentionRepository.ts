import Mention from "../domain/entities/mentionAggregate/Mention";

export default interface IMentionRepository {
  push(m: Mention): Promise<void>;
}