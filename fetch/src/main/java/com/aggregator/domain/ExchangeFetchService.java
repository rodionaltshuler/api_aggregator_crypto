package com.aggregator.domain;

public interface ExchangeFetchService {

    OrderBook fetchAll();

    OrderBook fetch(String market);

}
