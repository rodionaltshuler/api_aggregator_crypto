package com.aggregator.fetch.store;

import com.aggregator.domain.OrderBook;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.stream.Collectors;


@RedisHash("OrderBook")
public record OrderBookDto(@Id String exchangeId, List<MarketDto> markets) {

    public static OrderBookDto fromOrderBook(OrderBook book) {
        var markets =
                book.markets().values().stream().map(MarketDto::fromMarket).toList();

        return new OrderBookDto(
                book.exchangeId(),
                markets);
    }

    public OrderBook toOrderBook() {

        return new OrderBook(
                exchangeId(),
                markets.stream().collect(Collectors.toMap(
                        MarketDto::market,
                        MarketDto::toMarket))
        );

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
