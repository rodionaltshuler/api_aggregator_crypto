package com.aggregator.api.cache

import com.aggregator.store.OrderBookDto
import com.aggregator.store.OrderBookDtoRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
data class OrderBookDtoCache(
        val orderBooksByExchange : MutableMap<String, OrderBookDto> = ConcurrentHashMap<String, OrderBookDto>())

@Component
@Profile("!test")
class CacheUpdateScheduler(private val repository: OrderBookDtoRepository,
        private val cache: OrderBookDtoCache){

    @PostConstruct
    fun awaitForCacheFill(){
        //TODO service health to unhealthy
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    fun update() {
        repository.findAll().forEach { cache.orderBooksByExchange[it.exchangeId] = it }
        //TODO service health flag to healthy
    }
}