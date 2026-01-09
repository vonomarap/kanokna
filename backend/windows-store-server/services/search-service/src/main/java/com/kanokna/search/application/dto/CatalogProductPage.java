package com.kanokna.search.application.dto;

import java.util.Collections;
import java.util.List;

/**
 * Page of catalog products for reindex.
 */
public class CatalogProductPage {
    private final List<CatalogProductEvent> products;
    private final String nextPageToken;

    public CatalogProductPage(List<CatalogProductEvent> products, String nextPageToken) {
        this.products = products == null ? List.of() : List.copyOf(products);
        this.nextPageToken = nextPageToken;
    }

    public List<CatalogProductEvent> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public String getNextPageToken() {
        return nextPageToken;
    }
}
