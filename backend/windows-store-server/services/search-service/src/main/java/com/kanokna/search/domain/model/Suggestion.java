package com.kanokna.search.domain.model;

/**
 * Autocomplete suggestion entry.
 */
public record Suggestion(String text, SuggestionType type, String productId, long count, String highlighted) {
  public Suggestion(String text, SuggestionType type, String productId, long count, String highlighted) {
    this.text = text;
    this.type = type == null ? SuggestionType.UNSPECIFIED : type;
    this.productId = productId;
    this.count = count;
    this.highlighted = highlighted;
  }
}
