package com.kanokna.search.domain.model;

/**
 * Facet bucket value and count.
 */
public class FacetBucket {
    private final String key;
    private final String label;
    private final long count;
    private final boolean selected;

    public FacetBucket(String key, String label, long count, boolean selected) {
        this.key = key;
        this.label = label;
        this.count = count;
        this.selected = selected;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public long getCount() {
        return count;
    }

    public boolean isSelected() {
        return selected;
    }
}
