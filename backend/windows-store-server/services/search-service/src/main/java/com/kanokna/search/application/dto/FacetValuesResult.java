package com.kanokna.search.application.dto;

import com.kanokna.search.domain.model.FacetAggregation;

import java.util.Collections;
import java.util.List;

/**
 * Result of facet values retrieval.
 */
public record FacetValuesResult(List<FacetAggregation> facets, int queryTimeMs) {
  public FacetValuesResult(List<FacetAggregation> facets, int queryTimeMs) {
    this.facets = facets == null ? List.of() : List.copyOf(facets);
    this.queryTimeMs = queryTimeMs;
  }

  @Override
  public List<FacetAggregation> facets() {
    return Collections.unmodifiableList(facets);
  }
}
