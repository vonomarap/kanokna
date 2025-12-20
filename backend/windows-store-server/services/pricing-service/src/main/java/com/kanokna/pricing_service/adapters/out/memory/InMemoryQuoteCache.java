package com.kanokna.pricing_service.adapters.out.memory;

import com.kanokna.pricing_service.application.port.out.QuoteCache;
import com.kanokna.pricing_service.domain.model.Quote;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryQuoteCache implements QuoteCache {

    private final Map<String, Quote> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<Quote> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void put(String key, Quote quote) {
        if (key == null || key.isBlank()) {
            return;
        }
        cache.put(key, quote);
    }
}
