package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.Quote;
import java.util.Optional;

/**
 * Outbound port for quote caching.
 */
public interface QuoteCache {
    Optional<Quote> get(PriceBook priceBook, CalculateQuoteCommand command);

    void put(PriceBook priceBook, CalculateQuoteCommand command, Quote quote, int ttlMinutes);

    void evictByProductTemplateId(String productTemplateId);
}
