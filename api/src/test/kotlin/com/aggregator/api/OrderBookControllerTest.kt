package com.aggregator.api

import com.aggregator.api.cache.OrderBookDtoCache
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(OrderBookController::class)
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderBookControllerTest(@Autowired val mockMvc: MockMvc) {

    @Configuration
    class TestConfiguration {

        @Bean
        fun cache() : OrderBookDtoCache {
            val cache = OrderBookDtoCache()
            val exchange = "test-exchange"
            val markets = listOf(
                    MarketDto(1234567890, "BTC-USD", 0.1, 100.0, 0.11, 200.0))
            cache.orderBooksByExchange[exchange] = OrderBookDto(exchange, markets)
            println("Cache initialized")
            return cache
        }

        @Bean
        fun controller(): OrderBookController = OrderBookController(cache())
    }


    @Test
    fun `404 returned for an exchange with non-existing order book`() {
        mockMvc.perform(get("/exchanges/non-existing-exchange/order-books"))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `If no data for the market requested exists, order book with no markets is returned`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books?market=unknown"))
                .andExpect(jsonPath("$.markets.size()").value(0))
    }

    @Test
    fun `200 returned when order book is present for exchange we have data for`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books"))
                .andExpect(status().isOk)
    }

    @Test
    fun `Data for the market is present in data returned`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books"))
                .andExpect(jsonPath("$.markets[0].market").value("BTC-USD"))
                .andExpect(jsonPath("$.markets.size()").value(1))
    }

    @Test
    fun `Only bid values are exist when order_type=bid is requested`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books?order_type=bid"))
                .andExpect(jsonPath("$.markets[0].avgAsk").doesNotExist())
                .andExpect(jsonPath("$.markets[0].qtyAsk").doesNotExist())
                .andExpect(jsonPath("$.markets[0].avgBid").value(0.1))
                .andExpect(jsonPath("$.markets[0].qtyBid").value(100))
    }

    @Test
    fun `Only ask values are exist when order_type=ask is requested`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books?order_type=ask"))
                .andExpect(jsonPath("$.markets[0].avgAsk").value(0.11))
                .andExpect(jsonPath("$.markets[0].qtyAsk").value(200))
                .andExpect(jsonPath("$.markets[0].avgBid").doesNotExist())
                .andExpect(jsonPath("$.markets[0].qtyBid").doesNotExist())
    }

    @Test
    fun `Bot bid and ask values are are present if no order_type=ask or =bid requested`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books"))
                .andExpect(jsonPath("$.markets[0].avgAsk").value(0.11))
                .andExpect(jsonPath("$.markets[0].qtyAsk").value(200))
                .andExpect(jsonPath("$.markets[0].avgAsk").value(0.11))
                .andExpect(jsonPath("$.markets[0].qtyAsk").value(200))
    }

    @Test
    fun `Bot bid and ask values are are present if wrong order_type is requested`() {
        mockMvc.perform(get("/exchanges/test-exchange/order-books?order_type=sometype"))
                .andExpect(jsonPath("$.markets[0].avgAsk").value(0.11))
                .andExpect(jsonPath("$.markets[0].qtyAsk").value(200))
                .andExpect(jsonPath("$.markets[0].avgAsk").value(0.11))
                .andExpect(jsonPath("$.markets[0].qtyAsk").value(200))
    }

}