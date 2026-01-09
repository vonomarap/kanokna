package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.CreatePriceBookCommand;
import com.kanokna.pricing_service.domain.model.PriceBookId;

/**
 * Inbound port for creating price books (admin operation).
 */
public interface CreatePriceBookUseCase {
    PriceBookId createPriceBook(CreatePriceBookCommand command);
}

