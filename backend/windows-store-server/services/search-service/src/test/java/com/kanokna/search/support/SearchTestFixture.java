package com.kanokna.search.support;

import com.kanokna.search.application.dto.CatalogProductDeleteEvent;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.dto.CatalogProductPage;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.FacetAggregation;
import com.kanokna.search.domain.model.FacetBucket;
import com.kanokna.search.domain.model.FacetType;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.domain.model.Suggestion;
import com.kanokna.search.domain.model.SuggestionType;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import com.kanokna.test.fixtures.SearchFixtureDefaults;

import java.util.List;
import java.util.Map;

public final class SearchTestFixture {
    private SearchTestFixture() {
    }

    public static CatalogProductEvent catalogProductEvent(String productId, ProductStatus status) {
        return new CatalogProductEvent(
            SearchFixtureDefaults.EVENT_ID_PREFIX + productId,
            SearchFixtureDefaults.EVENT_TYPE_PUBLISHED,
            productId,
            SearchFixtureDefaults.PRODUCT_NAME_PREFIX + productId,
            SearchFixtureDefaults.PRODUCT_DESCRIPTION_PREFIX + productId,
            SearchFixtureDefaults.PRODUCT_FAMILY,
            SearchFixtureDefaults.PROFILE_SYSTEM,
            List.of(SearchFixtureDefaults.OPENING_TYPE),
            List.of(SearchFixtureDefaults.MATERIAL),
            List.of(SearchFixtureDefaults.COLOR),
            Money.ofMinor(SearchFixtureDefaults.MIN_PRICE_MINOR, Currency.RUB),
            Money.ofMinor(SearchFixtureDefaults.MAX_PRICE_MINOR, Currency.RUB),
            status,
            SearchFixtureDefaults.THUMBNAIL_BASE_URL + productId + ".png",
            SearchFixtureDefaults.POPULARITY,
            SearchFixtureDefaults.OPTION_GROUP_COUNT,
            SearchFixtureDefaults.now(),
            SearchFixtureDefaults.now()
        );
    }

    public static CatalogProductDeleteEvent deleteEvent(String productId) {
        return new CatalogProductDeleteEvent("event-" + productId, productId);
    }

    public static CatalogProductPage catalogProductPage(List<CatalogProductEvent> events, String nextToken) {
        return new CatalogProductPage(events, nextToken);
    }

    public static ProductSearchDocument productDocument(String productId, ProductStatus status) {
        return ProductSearchDocument.builder(productId)
            .name(LocalizedString.of(Language.RU, SearchFixtureDefaults.PRODUCT_NAME_PREFIX + productId))
            .description(LocalizedString.of(Language.RU, SearchFixtureDefaults.PRODUCT_DESCRIPTION_PREFIX + productId))
            .family(SearchFixtureDefaults.PRODUCT_FAMILY)
            .profileSystem(SearchFixtureDefaults.PROFILE_SYSTEM)
            .openingTypes(List.of(SearchFixtureDefaults.OPENING_TYPE))
            .materials(List.of(SearchFixtureDefaults.MATERIAL))
            .colors(List.of(SearchFixtureDefaults.COLOR))
            .minPrice(Money.ofMinor(SearchFixtureDefaults.MIN_PRICE_MINOR, Currency.RUB))
            .maxPrice(Money.ofMinor(SearchFixtureDefaults.MAX_PRICE_MINOR, Currency.RUB))
            .currency(SearchFixtureDefaults.CURRENCY_CODE)
            .popularity(SearchFixtureDefaults.DOCUMENT_POPULARITY)
            .status(status)
            .publishedAt(SearchFixtureDefaults.now())
            .thumbnailUrl(SearchFixtureDefaults.THUMBNAIL_BASE_URL + productId + ".png")
            .optionCount(SearchFixtureDefaults.OPTION_COUNT)
            .suggestInputs(List.of(SearchFixtureDefaults.PRODUCT_NAME_PREFIX + productId))
            .score(SearchFixtureDefaults.SCORE)
            .highlights(Map.of("name", SearchFixtureDefaults.HIGHLIGHT_NAME))
            .build();
    }

    public static SearchResult searchResult(List<ProductSearchDocument> documents, int page, int pageSize) {
        return new SearchResult(
            documents,
            documents.size(),
            page,
            pageSize,
            SearchFixtureDefaults.TOTAL_PAGES,
            List.of(),
            SearchFixtureDefaults.QUERY_TIME_MS
        );
    }

    public static FacetAggregation facetAggregation(String field, String key, long count, boolean selected) {
        return new FacetAggregation(
            field,
            field,
            FacetType.TERMS,
            List.of(new FacetBucket(key, key, count, selected))
        );
    }

    public static AutocompleteResult autocompleteResult(List<Suggestion> suggestions, int queryTimeMs) {
        return new AutocompleteResult(suggestions, List.of(), queryTimeMs);
    }

    public static Suggestion suggestion(String text, String productId) {
        return new Suggestion(
            text,
            SuggestionType.PRODUCT,
            productId,
            SearchFixtureDefaults.SUGGESTION_WEIGHT,
            text
        );
    }
}
