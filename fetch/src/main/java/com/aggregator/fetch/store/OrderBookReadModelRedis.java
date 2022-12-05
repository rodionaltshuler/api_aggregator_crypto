package com.aggregator.fetch.store;

import com.aggregator.domain.OrderBook;
import com.aggregator.domain.OrderBookReadModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderBookReadModelRedis implements OrderBookReadModel {

    private final OrderBookRepository repository;

    public OrderBookReadModelRedis(OrderBookRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderBook save(OrderBook orderBook) {

        var existing = repository.findById(orderBook.exchangeId())
                .map(OrderBookDto::toOrderBook);

        var bookToSave = existing
                .map(bookOld -> OrderBook.merge(bookOld, orderBook))
                .orElse(orderBook);

        var saved = repository.save(OrderBookDto.fromOrderBook(bookToSave));
        return saved.toOrderBook();
    }

    @Override
    public Optional<OrderBook> find(String exchangeId) {
        return repository.findById(exchangeId).map(OrderBookDto::toOrderBook);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
