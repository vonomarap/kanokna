package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.PriceBookId;

import java.util.Optional;

/**
 * Inbound port for querying price books (admin operations).
 */
public interface GetPriceBookQuery {
    Optional<PriceBook> getPriceBookById(PriceBookId id);
    Optional<PriceBook> getActivePriceBookForProduct(String productTemplateId);
}

