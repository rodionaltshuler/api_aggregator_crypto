package com.aggregator.store;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("OrderBookString")
public record OrderBookDtoRedis(@Id String exchangeId, String markets) {
}
