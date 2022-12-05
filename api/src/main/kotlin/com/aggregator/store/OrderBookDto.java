package com.aggregator.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;


@RedisHash("OrderBook")
public record OrderBookDto(@Id String exchangeId, List<MarketDto> markets) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MarketDto(long timestamp, String market, Double avgBid, Double qtyBid, Double avgAsk, Double qtyAsk) {
    }

}
