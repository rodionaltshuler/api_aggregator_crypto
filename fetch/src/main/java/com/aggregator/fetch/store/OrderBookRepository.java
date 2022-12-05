package com.aggregator.fetch.store;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends CrudRepository<OrderBookDto, String> {


}
