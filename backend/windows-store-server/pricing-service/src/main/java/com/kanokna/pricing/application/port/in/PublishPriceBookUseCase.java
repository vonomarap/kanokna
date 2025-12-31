package com.kanokna.pricing.application.port.in;

import com.kanokna.pricing.domain.model.PriceBookId;

import java.util.UUID;

/**
 * Inbound port for publishing price book versions (admin operation).
 */
public interface PublishPriceBookUseCase {
    UUID publishPriceBook(PriceBookId priceBookId, String publishedBy);
}
