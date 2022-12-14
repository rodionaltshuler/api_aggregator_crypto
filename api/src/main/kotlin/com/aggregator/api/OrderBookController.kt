package com.aggregator.api

import com.aggregator.api.cache.OrderBookDtoCache
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class OrderBookController(val cache: OrderBookDtoCache) {

    @GetMapping("/exchanges/{exchange_name}/order-books")
    fun getOrderBook(@PathVariable("exchange_name") exchangeName: String,
                     @RequestParam("market") marketParam: String?,
                     @RequestParam("order_type") orderTypeParam: String?): OrderBookDto {

        val book = cache.orderBooksByExchange[exchangeName]
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order book not found for exchange $exchangeName")

        var markets = book.markets
        //var markets = objectMapper.readValue<List<MarketDto>>(book.markets);

        marketParam?.let {
            markets = markets.filter { it.market == marketParam }
        }

        orderTypeParam?.let {
            markets =
                    when (it) {
                        "bid" -> markets.map { MarketDto(it.timestamp, it.market, it.avgBid, it.qtyBid, null, null) }
                        "ask" -> markets.map { MarketDto(it.timestamp, it.market, null, null, it.avgAsk, it.qtyAsk) }
                        else -> markets
                    }
        }

        return OrderBookDto(book.exchangeId, markets)
    }

}

data class OrderBookDto(val exchangeId: String, val markets: List<MarketDto>)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MarketDto(val timestamp: Long, val market: String, val avgBid: Double?, val qtyBid: Double?, val avgAsk: Double?, val qtyAsk: Double?)