package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Facet filter with selected values (OR within group).
 */
public record FacetFilter(String field, List<String> values) {
  public FacetFilter(String field, List<String> values) {
    this.field = Objects.requireNonNull(field, "field");
    this.values = values == null ? List.of() : List.copyOf(values);
  }

  @Override
  public List<String> values() {
    return Collections.unmodifiableList(values);
  }
}
