package com.kanokna.search.application.dto;

import com.kanokna.shared.i18n.Language;

import java.util.Collections;
import java.util.List;

/**
 * Query for facet values retrieval.
 */
public class GetFacetValuesQuery {
    private final List<String> fields;
    private final Language language;

    public GetFacetValuesQuery(List<String> fields, Language language) {
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        this.language = language;
    }

    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Language getLanguage() {
        return language;
    }
}
