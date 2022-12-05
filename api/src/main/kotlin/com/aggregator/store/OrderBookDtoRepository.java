package com.aggregator.store;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookDtoRepository extends CrudRepository<OrderBookDto, String> {


}
