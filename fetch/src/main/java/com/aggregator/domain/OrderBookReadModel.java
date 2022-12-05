package com.aggregator.domain;

import java.util.Optional;

public interface OrderBookReadModel {
    OrderBook save(OrderBook orderBook);

    Optional<OrderBook> find(String exchangeId);

    void deleteAll();
}
