package com.kanokna.search.application.dto;

import com.kanokna.shared.i18n.Language;

import java.util.Collections;
import java.util.List;

/**
 * Query for facet values retrieval.
 */
public record GetFacetValuesQuery(List<String> fields, Language language) {
  public GetFacetValuesQuery(List<String> fields, Language language) {
    this.fields = fields == null ? List.of() : List.copyOf(fields);
    this.language = language;
  }

  @Override
  public List<String> fields() {
    return Collections.unmodifiableList(fields);
  }
}
