package com.kanokna.pricing.application.port.out;

import com.kanokna.pricing.domain.model.PriceBook;
import java.util.Optional;

/**
 * Outbound port for price book persistence.
 */
public interface PriceBookRepository {
    Optional<PriceBook> findActiveByProductTemplateId(String productTemplateId);

    PriceBook save(PriceBook priceBook);
}
