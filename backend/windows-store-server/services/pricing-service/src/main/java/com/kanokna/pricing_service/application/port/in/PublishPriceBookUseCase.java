package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.domain.model.PriceBookId;

import java.util.UUID;

/**
 * Inbound port for publishing price book versions (admin operation).
 */
public interface PublishPriceBookUseCase {
    UUID publishPriceBook(PriceBookId priceBookId, String publishedBy);
}

