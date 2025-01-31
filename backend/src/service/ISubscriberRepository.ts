import Subscriber from "../domain/entities/subscriberAggregate/subscriber";
import IRepository from "../IRepository";

export default interface ISubscriberRepository extends IRepository<Subscriber, string> {}