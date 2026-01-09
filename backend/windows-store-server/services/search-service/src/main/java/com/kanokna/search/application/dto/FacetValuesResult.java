package com.kanokna.search.application.dto;

import com.kanokna.search.domain.model.FacetAggregation;

import java.util.Collections;
import java.util.List;

/**
 * Result of facet values retrieval.
 */
public class FacetValuesResult {
    private final List<FacetAggregation> facets;
    private final int queryTimeMs;

    public FacetValuesResult(List<FacetAggregation> facets, int queryTimeMs) {
        this.facets = facets == null ? List.of() : List.copyOf(facets);
        this.queryTimeMs = queryTimeMs;
    }

    public List<FacetAggregation> getFacets() {
        return Collections.unmodifiableList(facets);
    }

    public int getQueryTimeMs() {
        return queryTimeMs;
    }
}
