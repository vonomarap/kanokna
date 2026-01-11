package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.PriceBook;
import java.util.Optional;

/**
 * Outbound port for price book persistence.
 */
public interface PriceBookRepository {
    Optional<PriceBook> findActiveByProductTemplateId(String productTemplateId);

    PriceBook save(PriceBook priceBook);
}
