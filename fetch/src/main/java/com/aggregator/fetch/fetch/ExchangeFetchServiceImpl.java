package com.aggregator.fetch.fetch;

import com.aggregator.domain.OrderBook;
import com.aggregator.domain.ExchangeFetchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExchangeFetchServiceImpl implements ExchangeFetchService {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final WebClient webClient;

    private final String exchangeId;

    public ExchangeFetchServiceImpl(@Value("${exchange_id}") String exchangeId) {
        this.webClient = WebClient.create("https://api.blockchain.com/v3/exchange");
        this.exchangeId = exchangeId;
    }

    @Override
    public OrderBook fetch(String market) {
        return webClient.get()
                .uri("/l3/{market}", market)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OrderBookResponse.class)
                .map(r -> r.toOrderBook(exchangeId))
                .block();
    }

    @Override
    public OrderBook fetchAll() {

        var begin = Instant.now().toEpochMilli();
        System.out.println("Fetching all markets");

        var markets = fetchMarkets();

        System.out.println("Fetched " + markets.size() + " market symbols");
        var elapsed = Instant.now().toEpochMilli() - begin;
        System.out.println("Elapsed " + elapsed + " ms");

        var futures = markets.stream()
                        .map(market ->
                                CompletableFuture.supplyAsync(() -> fetch(market), executorService))
                        .toList();


        var mergedOrderBook = futures.stream()
                .map(CompletableFuture::join)
                .reduce(OrderBook::merge);

        System.out.println("Fetched " + markets.size() + " markets");
        elapsed = Instant.now().toEpochMilli() - begin;
        System.out.println("Elapsed " + elapsed + " ms");

        return mergedOrderBook.orElse(new OrderBook(exchangeId, Collections.emptyMap()));

    }

    private List<String> fetchMarkets() {

        return webClient.get()
                .uri("/symbols")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, SymbolStatus>>(){})
                .map(symbols -> symbols.entrySet().stream()
                        .filter(symbol -> symbol.getValue().status().equals("open"))
                        .map(Map.Entry::getKey)
                        .toList())
                .block();
    }


}
