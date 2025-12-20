package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.Quote;

import java.util.Optional;

public interface QuoteCache {

    Optional<Quote> get(String key);

    void put(String key, Quote quote);
}
