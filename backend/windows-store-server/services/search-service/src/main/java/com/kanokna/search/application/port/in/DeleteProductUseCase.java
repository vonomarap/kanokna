package com.kanokna.search.application.port.in;

import com.kanokna.search.application.dto.CatalogProductDeleteEvent;
import com.kanokna.search.application.dto.DeleteResult;

/**
 * Inbound port for deleting products from search index.
 */
public interface DeleteProductUseCase {
    DeleteResult deleteProduct(CatalogProductDeleteEvent event);
}
