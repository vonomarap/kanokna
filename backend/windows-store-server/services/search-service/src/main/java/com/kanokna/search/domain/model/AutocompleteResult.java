package com.kanokna.search.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Autocomplete suggestions result.
 */
public class AutocompleteResult {
    private final List<Suggestion> suggestions;
    private final List<Suggestion> categorySuggestions;
    private final int queryTimeMs;

    public AutocompleteResult(List<Suggestion> suggestions, List<Suggestion> categorySuggestions, int queryTimeMs) {
        this.suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
        this.categorySuggestions = categorySuggestions == null ? List.of() : List.copyOf(categorySuggestions);
        this.queryTimeMs = queryTimeMs;
    }

    public List<Suggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public List<Suggestion> getCategorySuggestions() {
        return Collections.unmodifiableList(categorySuggestions);
    }

    public int getQueryTimeMs() {
        return queryTimeMs;
    }
}
