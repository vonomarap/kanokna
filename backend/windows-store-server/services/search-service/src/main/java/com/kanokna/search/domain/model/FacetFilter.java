package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Facet filter with selected values (OR within group).
 */
public class FacetFilter {
    private final String field;
    private final List<String> values;

    public FacetFilter(String field, List<String> values) {
        this.field = Objects.requireNonNull(field, "field");
        this.values = values == null ? List.of() : List.copyOf(values);
    }

    public String getField() {
        return field;
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }
}
