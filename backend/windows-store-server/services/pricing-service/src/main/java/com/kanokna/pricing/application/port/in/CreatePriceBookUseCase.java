package com.kanokna.pricing.application.port.in;

import com.kanokna.pricing.application.dto.CreatePriceBookCommand;
import com.kanokna.pricing.domain.model.PriceBookId;

/**
 * Inbound port for creating price books (admin operation).
 */
public interface CreatePriceBookUseCase {
    PriceBookId createPriceBook(CreatePriceBookCommand command);
}

