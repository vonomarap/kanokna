package com.kanokna.pricing_service.adapters.out.memory;

import com.kanokna.pricing_service.application.port.out.PriceBookRepository;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPriceBookRepository implements PriceBookRepository {

    private final Map<String, PriceBook> store = new ConcurrentHashMap<>();

    @Override
    public Optional<PriceBook> findActiveById(Id priceBookId) {
        return Optional.ofNullable(store.get(priceBookId.value()))
            .filter(PriceBook::isActive);
    }

    @Override
    public Optional<PriceBook> findActiveByRegionAndCurrency(String region, Currency currency) {
        return store.values().stream()
            .filter(PriceBook::isActive)
            .filter(pb -> pb.region().equalsIgnoreCase(region) && pb.currency().equals(currency))
            .findFirst();
    }

    public void save(PriceBook priceBook) {
        store.put(priceBook.id().value(), priceBook);
    }
}
