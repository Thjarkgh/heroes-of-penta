import Subscriber from "../domain/entities/subscriberAggregate/Subscriber";
import IRepository from "../IRepository";

export default interface ISubscriberRepository extends IRepository<Subscriber, string> {}