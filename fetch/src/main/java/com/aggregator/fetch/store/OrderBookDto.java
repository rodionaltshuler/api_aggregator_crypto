package com.aggregator.fetch.store;

import com.aggregator.domain.OrderBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Arrays;
import java.util.stream.Collectors;


@RedisHash("OrderBookString")
public record OrderBookDto(@Id String exchangeId, String markets) {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static OrderBookDto fromOrderBook(OrderBook book) {
        var markets =
                book.markets().values().stream().map(MarketDto::fromMarket).toList();
        try {
            return new OrderBookDto(
                    book.exchangeId(),
                    mapper.writeValueAsString(markets));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderBook toOrderBook() {
        try {
            var marketsList = mapper.readValue(this.markets, OrderBookDto.MarketDto[].class);

            return new OrderBook(
                    exchangeId(),
                    Arrays.stream(marketsList).collect(Collectors.toMap(
                            MarketDto::market,
                            MarketDto::toMarket)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public record MarketDto(long timestamp, String market, double avgBid, double qtyBid, double avgAsk, double qtyAsk) {

        public static MarketDto fromMarket(OrderBook.Market market) {
            return new MarketDto(
                    market.timestamp(),
                    market.market(),
                    market.avgBid(), market.qtyBid(),
                    market.avgAsk(), market.qtyAsk());
        }

        public OrderBook.Market toMarket() {
            return new OrderBook.Market(
                    timestamp,
                    market,
                    avgBid,
                    qtyBid,
                    avgAsk,
                    qtyAsk);
        }

    }
}
