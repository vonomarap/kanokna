package com.kanokna.search.domain.model;

/**
 * Facet bucket value and count.
 */
public record FacetBucket(String key, String label, long count, boolean selected) {
}
