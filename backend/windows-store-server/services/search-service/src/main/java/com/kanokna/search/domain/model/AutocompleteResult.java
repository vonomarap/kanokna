package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Autocomplete suggestions result.
 */
public record AutocompleteResult(List<Suggestion> suggestions, List<Suggestion> categorySuggestions, int queryTimeMs) {
  public AutocompleteResult(List<Suggestion> suggestions, List<Suggestion> categorySuggestions, int queryTimeMs) {
    this.suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
    this.categorySuggestions = categorySuggestions == null ? List.of() : List.copyOf(categorySuggestions);
    this.queryTimeMs = queryTimeMs;
  }

  @Override
  public List<Suggestion> suggestions() {
    return Collections.unmodifiableList(suggestions);
  }

  @Override
  public List<Suggestion> categorySuggestions() {
    return Collections.unmodifiableList(categorySuggestions);
  }
}
