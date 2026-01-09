package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.Language;

/**
 * Autocomplete query parameters.
 */
public class AutocompleteQuery {
    private final String prefix;
    private final int limit;
    private final Language language;
    private final String familyFilter;

    public AutocompleteQuery(String prefix, int limit, Language language, String familyFilter) {
        this.prefix = prefix == null ? "" : prefix;
        this.limit = limit;
        this.language = language;
        this.familyFilter = familyFilter;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getLimit() {
        return limit;
    }

    public Language getLanguage() {
        return language;
    }

    public String getFamilyFilter() {
        return familyFilter;
    }
}
