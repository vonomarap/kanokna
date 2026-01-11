package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.Language;

/**
 * Autocomplete query parameters.
 */
public record AutocompleteQuery(String prefix, int limit, Language language, String familyFilter) {
  public AutocompleteQuery(String prefix, int limit, Language language, String familyFilter) {
    this.prefix = prefix == null ? "" : prefix;
    this.limit = limit;
    this.language = language;
    this.familyFilter = familyFilter;
  }
}
