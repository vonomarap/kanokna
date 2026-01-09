package com.kanokna.search.application.port.in;

import com.kanokna.search.application.dto.GetProductByIdQuery;
import com.kanokna.search.domain.model.ProductSearchDocument;

/**
 * Inbound port for fetching a product by id.
 */
public interface GetProductByIdUseCase {
    ProductSearchDocument getProductById(GetProductByIdQuery query);
}
