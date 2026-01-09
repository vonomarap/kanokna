package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Facet aggregation with buckets.
 */
public class FacetAggregation {
    private final String field;
    private final String displayName;
    private final FacetType type;
    private final List<FacetBucket> buckets;

    public FacetAggregation(String field, String displayName, FacetType type, List<FacetBucket> buckets) {
        this.field = Objects.requireNonNull(field, "field");
        this.displayName = displayName;
        this.type = type == null ? FacetType.UNSPECIFIED : type;
        this.buckets = buckets == null ? List.of() : List.copyOf(buckets);
    }

    public String getField() {
        return field;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FacetType getType() {
        return type;
    }

    public List<FacetBucket> getBuckets() {
        return Collections.unmodifiableList(buckets);
    }
}
