package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates search response data.
 */
public record SearchResult(List<ProductSearchDocument> products, long totalCount, int page, int pageSize,
                           int totalPages, List<FacetAggregation> facets, int queryTimeMs) {
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

  @Override
  public List<ProductSearchDocument> products() {
    return Collections.unmodifiableList(products);
  }

  @Override
  public List<FacetAggregation> facets() {
    return Collections.unmodifiableList(facets);
  }
}
