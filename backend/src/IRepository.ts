export default interface IRepository<TDomainAggregate, TDomainAggregateID> {
  get(id: TDomainAggregateID): Promise<TDomainAggregate>;
  has(id: TDomainAggregateID): Promise<boolean>;
  create(id: TDomainAggregateID): Promise<TDomainAggregate>;
  save(aggregate: TDomainAggregate): Promise<TDomainAggregateID>;
  delete(aggregate: TDomainAggregate): Promise<void>;
}