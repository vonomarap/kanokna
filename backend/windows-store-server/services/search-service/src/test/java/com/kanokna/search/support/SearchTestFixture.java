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

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class SearchTestFixture {
    private SearchTestFixture() {
    }

    public static CatalogProductEvent catalogProductEvent(String productId, ProductStatus status) {
        return new CatalogProductEvent(
            "event-" + productId,
            "PRODUCT_TEMPLATE_PUBLISHED",
            productId,
            "Window " + productId,
            "Description " + productId,
            "WINDOW",
            "REHAU",
            List.of("TILT"),
            List.of("PVC"),
            List.of("WHITE"),
            Money.ofMinor(100_00, Currency.RUB),
            Money.ofMinor(250_00, Currency.RUB),
            status,
            "http://example.com/" + productId + ".png",
            5,
            3,
            Instant.now(),
            Instant.now()
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
            .name(LocalizedString.of(Language.RU, "Window " + productId))
            .description(LocalizedString.of(Language.RU, "Description " + productId))
            .family("WINDOW")
            .profileSystem("REHAU")
            .openingTypes(List.of("TILT"))
            .materials(List.of("PVC"))
            .colors(List.of("WHITE"))
            .minPrice(Money.ofMinor(100_00, Currency.RUB))
            .maxPrice(Money.ofMinor(250_00, Currency.RUB))
            .currency("RUB")
            .popularity(10)
            .status(status)
            .publishedAt(Instant.now())
            .thumbnailUrl("http://example.com/" + productId + ".png")
            .optionCount(2)
            .suggestInputs(List.of("Window " + productId))
            .score(1.0f)
            .highlights(Map.of("name", "<em>Window</em>"))
            .build();
    }

    public static SearchResult searchResult(List<ProductSearchDocument> documents, int page, int pageSize) {
        return new SearchResult(
            documents,
            documents.size(),
            page,
            pageSize,
            1,
            List.of(),
            12
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
        return new Suggestion(text, SuggestionType.PRODUCT, productId, 1, text);
    }
}
