package com.kanokna.search.application.port.in;

import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;

/**
 * Inbound port for search products.
 */
public interface SearchProductsUseCase {
    SearchResult searchProducts(SearchQuery query);
}
