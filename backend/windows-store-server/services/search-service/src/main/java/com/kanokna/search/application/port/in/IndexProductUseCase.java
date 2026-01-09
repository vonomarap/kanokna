package com.kanokna.search.application.port.in;

import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.dto.IndexResult;

/**
 * Inbound port for indexing products.
 */
public interface IndexProductUseCase {
    IndexResult indexProduct(CatalogProductEvent event);
}
