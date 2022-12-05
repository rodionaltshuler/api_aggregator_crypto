package com.aggregator.fetch.fetch;

import com.aggregator.domain.ExchangeFetchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ExchangeFetchServiceIntegrationTest {

    @Autowired
    private ExchangeFetchService fetchService;

    @Value("${exchange_id}")
    private String exchangeId;

    @Test
    void fetchTest(){
        String market = "BTC-USD";
        var orderBook = fetchService.fetch(market);

        assert orderBook.exchangeId().equals(exchangeId);
        assert orderBook.markets().containsKey(market);
    }

    @Test
    void fetchAllTest(){
        var orderBook = fetchService.fetchAll();

        assert orderBook.exchangeId().equals(exchangeId);
        assert orderBook.markets().size() > 1;
    }
}
