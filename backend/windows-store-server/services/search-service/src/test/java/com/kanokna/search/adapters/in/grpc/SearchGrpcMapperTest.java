package com.kanokna.search.adapters.in.grpc;

import com.kanokna.search.domain.model.FacetAggregation;
import com.kanokna.search.domain.model.FacetBucket;
import com.kanokna.search.domain.model.FacetType;
import com.kanokna.search.domain.model.PriceRange;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.domain.model.SortField;
import com.kanokna.search.domain.model.SortOrder;
import com.kanokna.search.support.SearchTestFixture;
import com.kanokna.search.v1.AutocompleteRequest;
import com.kanokna.search.v1.ProductDocument;
import com.kanokna.search.v1.FacetFilter;
import com.kanokna.search.v1.PriceRangeFilter;
import com.kanokna.search.v1.SearchProductsRequest;
import com.kanokna.search.v1.SearchProductsResponse;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchGrpcMapperTest {
    private final SearchGrpcMapper mapper = new SearchGrpcMapper();

    @Test
    @DisplayName("TC-FUNC-SEARCH-003: toQuery_validSearchRequest_mapsCorrectly")
    void toQuery_validSearchRequest_mapsCorrectly() {
        SearchProductsRequest request = SearchProductsRequest.newBuilder()
            .setQuery("window")
            .setPage(1)
            .setPageSize(10)
            .setSortBy(com.kanokna.search.v1.SortField.SORT_FIELD_PRICE_ASC)
            .setSortOrder(com.kanokna.search.v1.SortOrder.SORT_ORDER_ASC)
            .addFilters(FacetFilter.newBuilder()
                .setField("family")
                .addValues("WINDOW")
                .build())
            .setPriceRange(PriceRangeFilter.newBuilder()
                .setMinPrice(1000)
                .setMaxPrice(5000)
                .setCurrency("RUB")
                .build())
            .setLanguage("ru")
            .build();

        SearchQuery query = mapper.toQuery(request);

        assertEquals("window", query.queryText());
        assertEquals(1, query.page());
        assertEquals(10, query.pageSize());
        assertEquals(SortField.PRICE_ASC, query.sortField());
        assertEquals(SortOrder.ASC, query.sortOrder());
        assertEquals("family", query.filters().get(0).field());
      assertInstanceOf(PriceRange.class, query.priceRange());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-001: toQuery_validAutocompleteRequest_mapsCorrectly")
    void toQuery_validAutocompleteRequest_mapsCorrectly() {
        AutocompleteRequest request = AutocompleteRequest.newBuilder()
            .setPrefix("wi")
            .setLimit(5)
            .setLanguage("en")
            .setFamilyFilter("WINDOW")
            .build();

        var query = mapper.toQuery(request);

        assertEquals("wi", query.prefix());
        assertEquals(5, query.limit());
        assertEquals(Language.EN, query.language());
        assertEquals("WINDOW", query.familyFilter());
    }

    @Test
    @DisplayName("TC-FUNC-GET-001: toProductDocument_validDocument_mapsAllFields")
    void toProductDocument_validDocument_mapsAllFields() {
        ProductSearchDocument document = ProductSearchDocument.builder("p1")
            .name(LocalizedString.of(Language.RU, "Window"))
            .description(LocalizedString.of(Language.RU, "Description"))
            .family("WINDOW")
            .profileSystem("REHAU")
            .openingTypes(List.of("TILT"))
            .materials(List.of("PVC"))
            .colors(List.of("WHITE"))
            .minPrice(Money.ofMinor(100_00, Currency.RUB))
            .maxPrice(Money.ofMinor(200_00, Currency.RUB))
            .currency("RUB")
            .popularity(5)
            .status(ProductStatus.ACTIVE)
            .thumbnailUrl("http://example.com/p1.png")
            .optionCount(2)
            .highlights(Map.of("name", "<em>Window</em>"))
            .build();

        ProductDocument proto = mapper.toProductDocument(document, Language.RU);

        assertEquals("p1", proto.getId());
        assertEquals("Window", proto.getName());
        assertEquals("WINDOW", proto.getFamily());
        assertEquals(10000, proto.getMinPrice().getAmountMinor());
        assertEquals(20000, proto.getMaxPrice().getAmountMinor());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-008: toResponse_searchResult_includesFacets")
    void toResponse_searchResult_includesFacets() {
        FacetAggregation aggregation = new FacetAggregation(
            "family",
            "family",
            FacetType.TERMS,
            List.of(new FacetBucket("WINDOW", "WINDOW", 3, true))
        );
        SearchResult result = new SearchResult(
            List.of(SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE)),
            1,
            0,
            20,
            1,
            List.of(aggregation),
            5
        );

        SearchProductsResponse response = mapper.toResponse(result, Language.RU);

        assertEquals(1, response.getFacetsCount());
        assertEquals("family", response.getFacets(0).getField());
    }
}
