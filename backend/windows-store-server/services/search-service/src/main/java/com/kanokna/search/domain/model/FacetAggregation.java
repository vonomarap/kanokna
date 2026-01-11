package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Facet aggregation with buckets.
 */
public record FacetAggregation(String field, String displayName, FacetType type, List<FacetBucket> buckets) {
  public FacetAggregation(String field, String displayName, FacetType type, List<FacetBucket> buckets) {
    this.field = Objects.requireNonNull(field, "field");
    this.displayName = displayName;
    this.type = type == null ? FacetType.UNSPECIFIED : type;
    this.buckets = buckets == null ? List.of() : List.copyOf(buckets);
  }

  @Override
  public List<FacetBucket> buckets() {
    return Collections.unmodifiableList(buckets);
  }
}
