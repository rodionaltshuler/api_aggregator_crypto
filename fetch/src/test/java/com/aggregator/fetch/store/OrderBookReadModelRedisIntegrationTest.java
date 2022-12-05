package com.aggregator.fetch.store;

import com.aggregator.domain.OrderBook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class OrderBookReadModelRedisIntegrationTest {

    @Container
    private  FixedHostPortGenericContainer redisContainer =
            new FixedHostPortGenericContainer("redis:7.0.5-alpine3.17")
                    .withFixedExposedPort(6379, 6379);
    @Autowired
    private OrderBookReadModelRedis readModel;

    @AfterEach
    void setup() {
        readModel.deleteAll();
    }

    @Test
    void save_savedMatchesRetrieved() {

        var exchange = UUID.randomUUID().toString();
        final var market = UUID.randomUUID().toString();
        final var orderBook = sampleOrderBook(exchange, Instant.now().toEpochMilli(), market);
        readModel.save(orderBook);
        var retrieved = readModel.find(orderBook.exchangeId())
                .orElseThrow(() -> new RuntimeException("Order book not present in repository for exchange " + orderBook.exchangeId()));
        assert orderBook.equals(retrieved);
    }

    @Test
    void save_latestValueIsRetrievedAfterMultipleSave() throws Throwable {

        var earlierTimestamp = Instant.now().toEpochMilli();
        var laterTimestamp = earlierTimestamp + 10000;

        var market1 = "alpha";
        var market2 = "beta";
        var exchange = UUID.randomUUID().toString();


        final var orderBookOld = sampleOrderBook(exchange, earlierTimestamp, market1, market2);

        final var orderBookFresh = sampleOrderBook(exchange, laterTimestamp, market1, market2);

        //emphasizing we're updating data for an exchange, not adding data for another one
        assert orderBookOld.exchangeId().equals(orderBookFresh.exchangeId());

        readModel.save(orderBookOld);
        readModel.save(orderBookFresh);

        var retrieved = readModel.find(orderBookFresh.exchangeId())
                .orElseThrow(() -> new RuntimeException("Order book not present in repository for exchange " + orderBookFresh.exchangeId()));

        assert retrieved.markets().get(market1).timestamp() == laterTimestamp;
    }

    @Test
    void save_latestValueIsRetrievedAfterMultipleSave_evenIfFresherDataComesLater() {

        var earlierTimestamp = Instant.now().toEpochMilli();
        var laterTimestamp = earlierTimestamp + 10000;

        var exchange = UUID.randomUUID().toString();
        var market1 = "alpha";

        final var orderBookOld = sampleOrderBook(exchange, earlierTimestamp, market1);
        final var orderBookFresh = sampleOrderBook(exchange, laterTimestamp, market1);


        var exchangeId = orderBookFresh.exchangeId();

        readModel.save(orderBookFresh);
        readModel.save(orderBookOld);

        var retrieved = readModel.find(orderBookFresh.exchangeId())
                .orElseThrow(() -> new RuntimeException("Order book not present in repository for exchange " + orderBookFresh.exchangeId()));

        assert retrieved.find(market1).get().timestamp() == laterTimestamp;
    }

    @Test
    void save_onlyMarketsWithFresherTimestampAreUpdated() {

        var earlierTimestamp = Instant.now().toEpochMilli();
        var laterTimestamp = earlierTimestamp + 10000;
        var moreEarlierTimestamp = earlierTimestamp - 1000;

        var market1name = "market1";
        var market2name = "market2";

        var exchangeId = "some_exchange";

        var market1_old = new OrderBook.Market(earlierTimestamp, market1name, 100, 400, 105, 500);
        var market2_old = new OrderBook.Market(earlierTimestamp, market2name, 0.120, 0.400, 0.105, 0.500);

        var orderBookOld = new OrderBook(exchangeId, Map.of(market1name, market1_old, market2name, market2_old));
        readModel.save(orderBookOld);

        var market1_fresh = new OrderBook.Market(laterTimestamp, market1name, 200, 500, 205, 600);
        var market2_older = new OrderBook.Market(moreEarlierTimestamp, market2name, 0.120, 0.400, 0.105, 0.500);
        var orderBookPartiallyFresh = new OrderBook(exchangeId, Map.of(market1name, market1_fresh, market2name, market2_older));
        readModel.save(orderBookPartiallyFresh);

        var retained = readModel.find(exchangeId).orElseThrow();

        var market1Retrieved = retained.find(market1name).get();
        assert market1Retrieved.timestamp() == laterTimestamp;

        var market2Retrieved = retained.find(market2name).get();
        assert market2Retrieved.timestamp() == earlierTimestamp;
    }

    @Test
    void save_oldMarketDataRetained_ifFreshMissing() {
        var earlierTimestamp = Instant.now().toEpochMilli();
        var laterTimestamp = earlierTimestamp + 10000;
        var exchangeId = "some_exchange";
        var market1name = "market1";
        var market2name = "market2";

        var market1_old = new OrderBook.Market(earlierTimestamp, market1name, 100, 400, 105, 500);
        var market2_old = new OrderBook.Market(earlierTimestamp, market2name, 0.120, 0.400, 0.105, 0.500);

        var orderBookOld = new OrderBook(exchangeId, Map.of(market1name, market1_old, market2name, market2_old));
        readModel.save(orderBookOld);

        var market1_fresh = new OrderBook.Market(laterTimestamp, market1name, 200, 500, 205, 600);

        var orderBookFresh = new OrderBook(exchangeId, Map.of(market1name, market1_fresh));
        readModel.save(orderBookFresh);

        var retained = readModel.find(exchangeId).orElseThrow();

        assert retained.find(market2name).isPresent();

    }

    private OrderBook sampleOrderBook(String exchangeId, long timestamp, String... marketNames) {
        var marketsMap = Arrays.stream(marketNames)
                .map(name -> new OrderBook.Market(timestamp, name, 100, 400, 105, 500))
                .collect(Collectors.toMap(
                        OrderBook.Market::market,
                        market -> market
                ));

        return new OrderBook(
                exchangeId,
                marketsMap
        );
    }


}
