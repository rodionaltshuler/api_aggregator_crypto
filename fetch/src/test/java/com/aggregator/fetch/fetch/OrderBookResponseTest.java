package com.aggregator.fetch.fetch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBookResponseTest {

    @ParameterizedTest
    @CsvSource(value = {
            "1:100:3:100:2:200",
            "0.01:100:0.03:100:0.02:200",
            "0:0:0:0:0:0"
    }, delimiter = ':')
    public void toOrderBook_Test(double px1, double qty1, double px2, double qty2,
                                double expectedAvg, double expectedQty){

        var market = "market1";
        var exchange = "exchange1";

        var bids = List.of(
                new OrderBookEntry(px1, qty1),
                new OrderBookEntry(px2, qty2)
        );

        var asks = new ArrayList<>(bids);

        var response = new OrderBookResponse(market, bids, asks);

        var orderBook = response.toOrderBook(exchange);

        assert orderBook.markets().get(market)
                .avgBid() == expectedAvg;

        assert orderBook.markets().get(market)
                .qtyBid() == expectedQty;

        assert orderBook.markets().get(market)
                .avgAsk() == expectedAvg;

        assert orderBook.markets().get(market)
                .qtyAsk() == expectedQty;

    }

    @Test
    public void toOrderBook_EmptyBook_Test(){

        var market = "market1";
        var exchange = "exchange1";

        var response = new OrderBookResponse(market,
                Collections.emptyList(),
                Collections.emptyList());

        var orderBook = response.toOrderBook(exchange);

        assert orderBook.markets().get(market)
                .avgBid() == 0;

        assert orderBook.markets().get(market)
                .qtyBid() == 0;

        assert orderBook.markets().get(market)
                .avgAsk() == 0;

        assert orderBook.markets().get(market)
                .qtyAsk() == 0;

    }
}
