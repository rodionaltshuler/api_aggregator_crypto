package com.aggregator.fetch.fetch;

import com.aggregator.domain.ExchangeFetchService;
import com.aggregator.domain.OrderBook;
import com.aggregator.domain.OrderBookReadModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.mockito.Mockito.*;

public class UpdateSchedulerTest {

    @Mock
    private OrderBookReadModel readModel;

    @Mock
    private ExchangeFetchService fetchService;

    @Value("${exchange_id}")
    private String exchangeId;

    private AutoCloseable mocksCloseable;
    private UpdateScheduler updateScheduler;

    @BeforeEach
    void prepare() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        updateScheduler = new UpdateScheduler(fetchService, readModel);
    }

    @AfterEach
    void cleanup() {
        try {
            mocksCloseable.close();
        } catch (Exception ignored) {}

        readModel.deleteAll();
    }

    @Test
    void updateOrderBookTest() {
        var timestamp = 1234567890;

        var marketAlphaName = "alpha-market";
        var marketAlpha = new OrderBook.Market(timestamp,
                marketAlphaName,
                0.01, 100, 0.01, 100);

        var marketBetaName = "beta-market";
        var marketBeta = new OrderBook.Market(timestamp,
                marketBetaName,
                0.02, 200, 0.02, 200);


        var mockBook = new OrderBook(exchangeId, Map.of(marketAlphaName, marketAlpha, marketBetaName, marketBeta));

        when(fetchService.fetchAll())
                .thenReturn(mockBook);

        updateScheduler.updateOrderBook();

        //verify we made call to ExchangeFetchService
        verify(fetchService, times(1)).fetchAll();

        //verify we made call to OrderBookReadModel with correct argument
        var argCaptor = ArgumentCaptor.forClass(OrderBook.class);
        verify(readModel, times(1))
                .save(argCaptor.capture());

        var bookToSave = argCaptor.getValue();

        assert bookToSave.markets().containsKey(marketAlphaName);
        assert bookToSave.markets().containsKey(marketBetaName);
        assert bookToSave.markets().size() == 2;

    }

}
