package com.kanokna.search.application.dto;

import java.util.Collections;
import java.util.List;

/**
 * Page of catalog products for reindex.
 */
public record CatalogProductPage(List<CatalogProductEvent> products, String nextPageToken) {
  public CatalogProductPage(List<CatalogProductEvent> products, String nextPageToken) {
    this.products = products == null ? List.of() : List.copyOf(products);
    this.nextPageToken = nextPageToken;
  }

  @Override
  public List<CatalogProductEvent> products() {
    return Collections.unmodifiableList(products);
  }
}
