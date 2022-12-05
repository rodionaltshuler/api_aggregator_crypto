package com.aggregator.fetch.fetch;

import com.aggregator.domain.OrderBook;
import com.aggregator.domain.ExchangeFetchService;
import com.aggregator.domain.OrderBookReadModel;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Profile("!test")
record UpdateScheduler(ExchangeFetchService fetchService, OrderBookReadModel readModel) {

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void updateOrderBook() {
        OrderBook.fetchAndSaveFresh(fetchService, readModel);
    }
}
