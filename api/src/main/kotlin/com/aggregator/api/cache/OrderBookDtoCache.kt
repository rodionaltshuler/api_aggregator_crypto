package com.aggregator.api.cache

import com.aggregator.api.OrderBookDto
import com.aggregator.store.OrderBookDtoRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
        private val cache: OrderBookDtoCache,
        private val objectMapper: ObjectMapper){

    @PostConstruct
    fun awaitForCacheFill(){
        //TODO service health to unhealthy
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    fun update() {
        repository.findAll()
                .map { entry -> OrderBookDto(entry.exchangeId, objectMapper.readValue(entry.markets) ) }
                .forEach { cache.orderBooksByExchange[it.exchangeId] = it }
        //TODO service health flag to healthy
    }
}