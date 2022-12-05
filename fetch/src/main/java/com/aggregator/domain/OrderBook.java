package com.aggregator.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record OrderBook(String exchangeId, Map<String, Market> markets) {

    public OrderBook(String exchangeId, Map<String, Market> markets) {
        this.exchangeId = exchangeId;
        this.markets = Collections.unmodifiableMap(markets);
    }

    public static OrderBook fetchAndSaveFresh(ExchangeFetchService fetchService, OrderBookReadModel readModel) {
        var orderBook = fetchService.fetchAll();
        return readModel.save(orderBook);
    }
    
    public Optional<Market> find(String market){
        return Optional.ofNullable(markets.get(market));
    }

    public static OrderBook merge(OrderBook first, OrderBook second){

        assert first.exchangeId.equals(second.exchangeId);

        var markets = first.markets;

        var merged = new HashMap<String, Market>();

        second.markets().forEach((key, value) -> {
            var existingValue = markets.get(key);
            if (existingValue == null || value.timestamp() >= existingValue.timestamp()) {
                merged.put(key, value);
            } else {
                merged.put(existingValue.market(), existingValue);
            }
        });

        markets.forEach(merged::putIfAbsent);

        return new OrderBook(second.exchangeId(), merged);
    }


    public record Market(long timestamp, String market, double avgBid, double qtyBid, double avgAsk, double qtyAsk) {

    }

}


