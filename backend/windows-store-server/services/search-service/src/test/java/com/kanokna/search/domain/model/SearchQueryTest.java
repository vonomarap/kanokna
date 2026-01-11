package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryTest {
    @Test
    @DisplayName("TC-FUNC-SEARCH-001: Empty query defaults to match-all inputs")
    void emptyQuery_defaultsToEmptyStringAndNoFilters() {
        SearchQuery query = new SearchQuery(
            null,
            0,
            20,
            SortField.RELEVANCE,
            SortOrder.ASC,
            null,
            null,
            Language.RU
        );

        assertEquals("", query.queryText());
        assertTrue(query.filters().isEmpty());
        assertEquals(0, query.page());
        assertEquals(20, query.pageSize());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-005: Multiple filters are preserved for AND logic")
    void multipleFilters_preservedForAndLogic() {
        FacetFilter family = new FacetFilter("family", List.of("WINDOW"));
        FacetFilter material = new FacetFilter("materials", List.of("PVC"));

        SearchQuery query = new SearchQuery(
            "window",
            1,
            10,
            SortField.RELEVANCE,
            SortOrder.DESC,
            List.of(family, material),
            null,
            Language.EN
        );

        assertEquals(2, query.filters().size());
        assertEquals("family", query.filters().get(0).field());
        assertEquals("materials", query.filters().get(1).field());
    }
}
