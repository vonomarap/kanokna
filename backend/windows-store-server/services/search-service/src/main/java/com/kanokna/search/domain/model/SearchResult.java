package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates search response data.
 */
public class SearchResult {
    private final List<ProductSearchDocument> products;
    private final long totalCount;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final List<FacetAggregation> facets;
    private final int queryTimeMs;

    public SearchResult(
        List<ProductSearchDocument> products,
        long totalCount,
        int page,
        int pageSize,
        int totalPages,
        List<FacetAggregation> facets,
        int queryTimeMs
    ) {
        this.products = products == null ? List.of() : List.copyOf(products);
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.facets = facets == null ? List.of() : List.copyOf(facets);
        this.queryTimeMs = queryTimeMs;
    }

    public List<ProductSearchDocument> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<FacetAggregation> getFacets() {
        return Collections.unmodifiableList(facets);
    }

    public int getQueryTimeMs() {
        return queryTimeMs;
    }
}
