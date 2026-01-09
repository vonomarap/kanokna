package com.kanokna.search.domain.model;

/**
 * Autocomplete suggestion entry.
 */
public class Suggestion {
    private final String text;
    private final SuggestionType type;
    private final String productId;
    private final long count;
    private final String highlighted;

    public Suggestion(String text, SuggestionType type, String productId, long count, String highlighted) {
        this.text = text;
        this.type = type == null ? SuggestionType.UNSPECIFIED : type;
        this.productId = productId;
        this.count = count;
        this.highlighted = highlighted;
    }

    public String getText() {
        return text;
    }

    public SuggestionType getType() {
        return type;
    }

    public String getProductId() {
        return productId;
    }

    public long getCount() {
        return count;
    }

    public String getHighlighted() {
        return highlighted;
    }
}
