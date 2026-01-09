package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.Language;

import java.util.Collections;
import java.util.List;

/**
 * Value object encapsulating search request parameters.
 */
public class SearchQuery {
    private final String queryText;
    private final int page;
    private final int pageSize;
    private final SortField sortField;
    private final SortOrder sortOrder;
    private final List<FacetFilter> filters;
    private final PriceRange priceRange;
    private final Language language;

    public SearchQuery(
        String queryText,
        int page,
        int pageSize,
        SortField sortField,
        SortOrder sortOrder,
        List<FacetFilter> filters,
        PriceRange priceRange,
        Language language
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        this.queryText = queryText == null ? "" : queryText;
        this.page = page;
        this.pageSize = pageSize;
        this.sortField = sortField == null ? SortField.UNSPECIFIED : sortField;
        this.sortOrder = sortOrder == null ? SortOrder.UNSPECIFIED : sortOrder;
        this.filters = filters == null ? List.of() : List.copyOf(filters);
        this.priceRange = priceRange;
        this.language = language;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public SortField getSortField() {
        return sortField;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public List<FacetFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public Language getLanguage() {
        return language;
    }
}
