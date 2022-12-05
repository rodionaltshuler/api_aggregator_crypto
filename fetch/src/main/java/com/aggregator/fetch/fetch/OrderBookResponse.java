package com.aggregator.fetch.fetch;

import com.aggregator.domain.OrderBook;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record OrderBookResponse(@JsonProperty("symbol") String symbol,
                                @JsonProperty("bids") List<OrderBookEntry> bids,
                                @JsonProperty("asks") List<OrderBookEntry> asks){

    public OrderBook toOrderBook(String exchangeId){
        var sumAsk = 0.0;
        var qtyAsk = 0.0;
        var avgAsk = 0.0;

        for (OrderBookEntry e : asks()) {
            qtyAsk += e.qty();
            sumAsk += e.qty() * e.px();
        }

        avgAsk = qtyAsk!= 0 ? sumAsk / qtyAsk : 0;

        var sumBid = 0.0;
        var qtyBid = 0.0;
        var avgBid = 0.0;

        for (OrderBookEntry e : bids()) {
            qtyBid += e.qty();
            sumBid += e.qty() * e.px();
        }

        avgBid = qtyBid != 0 ? sumBid / qtyBid : 0;

        var market = new OrderBook.Market(Instant.now().toEpochMilli(),
                symbol,
                avgBid, qtyBid, avgAsk, qtyAsk);

        return new OrderBook(exchangeId, Map.of(market.market(), market));
    }
}


