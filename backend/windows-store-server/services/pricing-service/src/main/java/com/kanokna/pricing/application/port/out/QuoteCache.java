package com.kanokna.pricing.application.port.out;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.domain.model.PriceBook;
import com.kanokna.pricing.domain.model.Quote;
import java.util.Optional;

/**
 * Outbound port for quote caching.
 */
public interface QuoteCache {
    Optional<Quote> get(PriceBook priceBook, CalculateQuoteCommand command);

    void put(PriceBook priceBook, CalculateQuoteCommand command, Quote quote, int ttlMinutes);

    void evictByProductTemplateId(String productTemplateId);
}
