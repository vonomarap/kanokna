package com.kanokna.search.adapters.in.grpc;

import com.google.protobuf.Timestamp;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.GetProductByIdQuery;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.FacetAggregation;
import com.kanokna.search.domain.model.FacetBucket;
import com.kanokna.search.domain.model.FacetFilter;
import com.kanokna.search.domain.model.FacetType;
import com.kanokna.search.domain.model.PriceRange;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.domain.model.SortField;
import com.kanokna.search.domain.model.SortOrder;
import com.kanokna.search.domain.model.Suggestion;
import com.kanokna.search.domain.model.SuggestionType;
import com.kanokna.search.v1.AutocompleteRequest;
import com.kanokna.search.v1.AutocompleteResponse;
import com.kanokna.search.v1.GetFacetValuesRequest;
import com.kanokna.search.v1.GetFacetValuesResponse;
import com.kanokna.search.v1.GetProductByIdRequest;
import com.kanokna.search.v1.ProductDocument;
import com.kanokna.search.v1.SearchProductsRequest;
import com.kanokna.search.v1.SearchProductsResponse;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Mapper between search domain models and gRPC messages.
 */
@Component
public class SearchGrpcMapper {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public SearchQuery toQuery(SearchProductsRequest request) {
        int page = request.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        int pageSize = request.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : request.getPageSize();
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }

        List<FacetFilter> filters = request.getFiltersList().stream()
            .map(filter -> new FacetFilter(filter.getField(), filter.getValuesList()))
            .toList();

        PriceRange priceRange = toPriceRange(request);

        return new SearchQuery(
            request.getQuery(),
            page,
            pageSize,
            mapSortField(request.getSortBy()),
            mapSortOrder(request.getSortOrder()),
            filters,
            priceRange,
            mapLanguage(request.getLanguage())
        );
    }

    public AutocompleteQuery toQuery(AutocompleteRequest request) {
        return new AutocompleteQuery(
            request.getPrefix(),
            request.getLimit(),
            mapLanguage(request.getLanguage()),
            blankToNull(request.getFamilyFilter())
        );
    }

    public GetFacetValuesQuery toQuery(GetFacetValuesRequest request) {
        return new GetFacetValuesQuery(request.getFieldsList(), mapLanguage(request.getLanguage()));
    }

    public GetProductByIdQuery toQuery(GetProductByIdRequest request) {
        return new GetProductByIdQuery(request.getProductId(), mapLanguage(request.getLanguage()));
    }

    public SearchProductsResponse toResponse(SearchResult result, Language language) {
        SearchProductsResponse.Builder builder = SearchProductsResponse.newBuilder()
            .setTotalCount(result.totalCount())
            .setPage(result.page())
            .setPageSize(result.pageSize())
            .setTotalPages(result.totalPages())
            .setQueryTimeMs(result.queryTimeMs());

        for (ProductSearchDocument document : result.products()) {
            builder.addProducts(toProductDocument(document, language));
        }
        for (FacetAggregation facet : result.facets()) {
            builder.addFacets(toFacetAggregation(facet));
        }
        return builder.build();
    }

    public AutocompleteResponse toResponse(AutocompleteResult result) {
        AutocompleteResponse.Builder builder = AutocompleteResponse.newBuilder()
            .setQueryTimeMs(result.queryTimeMs());

        for (Suggestion suggestion : result.suggestions()) {
            builder.addSuggestions(toSuggestion(suggestion));
        }
        for (Suggestion suggestion : result.categorySuggestions()) {
            builder.addCategorySuggestions(toSuggestion(suggestion));
        }
        return builder.build();
    }

    public GetFacetValuesResponse toResponse(FacetValuesResult result) {
        GetFacetValuesResponse.Builder builder = GetFacetValuesResponse.newBuilder();
        for (FacetAggregation facet : result.facets()) {
            builder.addFacets(toFacetAggregation(facet));
        }
        return builder.build();
    }

    public ProductDocument toProductDocument(ProductSearchDocument document, Language language) {
        ProductDocument.Builder builder = ProductDocument.newBuilder()
            .setId(document.getId())
            .setName(resolveLocalized(document.getName(), language))
            .setDescription(resolveLocalized(document.getDescription(), language))
            .setFamily(blankToEmpty(document.getFamily()))
            .setProfileSystem(blankToEmpty(document.getProfileSystem()))
            .addAllOpeningTypes(document.getOpeningTypes())
            .addAllMaterials(document.getMaterials())
            .addAllColors(document.getColors())
            .setPopularity(document.getPopularity())
            .setStatus(mapStatus(document.getStatus()))
            .setThumbnailUrl(blankToEmpty(document.getThumbnailUrl()))
            .setOptionCount(document.getOptionCount())
            .putAllHighlights(document.getHighlights());

        if (document.getMinPrice() != null) {
            builder.setMinPrice(toMoney(document.getMinPrice()));
        }
        if (document.getMaxPrice() != null) {
            builder.setMaxPrice(toMoney(document.getMaxPrice()));
        }
        if (document.getPublishedAt() != null) {
            builder.setPublishedAt(toTimestamp(document.getPublishedAt()));
        }
        if (document.getScore() != null) {
            builder.setScore(document.getScore());
        }
        return builder.build();
    }

    private com.kanokna.search.v1.FacetAggregation toFacetAggregation(FacetAggregation aggregation) {
        com.kanokna.search.v1.FacetAggregation.Builder builder = com.kanokna.search.v1.FacetAggregation.newBuilder()
            .setField(aggregation.field())
            .setDisplayName(blankToEmpty(aggregation.displayName()))
            .setType(mapFacetType(aggregation.type()));

        for (FacetBucket bucket : aggregation.buckets()) {
            builder.addBuckets(com.kanokna.search.v1.FacetBucket.newBuilder()
                .setKey(blankToEmpty(bucket.key()))
                .setLabel(blankToEmpty(bucket.label()))
                .setCount(bucket.count())
                .setSelected(bucket.selected())
                .build());
        }
        return builder.build();
    }

    private com.kanokna.search.v1.Suggestion toSuggestion(Suggestion suggestion) {
        return com.kanokna.search.v1.Suggestion.newBuilder()
            .setText(blankToEmpty(suggestion.text()))
            .setType(mapSuggestionType(suggestion.type()))
            .setProductId(blankToEmpty(suggestion.productId()))
            .setCount(suggestion.count())
            .setHighlighted(blankToEmpty(suggestion.highlighted()))
            .build();
    }

    private PriceRange toPriceRange(SearchProductsRequest request) {
        if (!request.hasPriceRange()) {
            return null;
        }
        var filter = request.getPriceRange();
        if (filter.getMinPrice() == 0 && filter.getMaxPrice() == 0 && blankToNull(filter.getCurrency()) == null) {
            return null;
        }
        String currencyText = blankToNull(filter.getCurrency());
        if (currencyText == null) {
            throw new IllegalArgumentException("priceRange.currency is required");
        }
        Currency currency = mapCurrency(currencyText);
        Money min = filter.getMinPrice() > 0
            ? Money.ofMinor(filter.getMinPrice(), currency)
            : null;
        Money max = filter.getMaxPrice() > 0
            ? Money.ofMinor(filter.getMaxPrice(), currency)
            : null;
        if (min == null && max == null) {
            return null;
        }
        return new PriceRange(min, max);
    }

    private SortField mapSortField(com.kanokna.search.v1.SortField sortField) {
        if (sortField == null) {
            return SortField.UNSPECIFIED;
        }
        return switch (sortField) {
            case SORT_FIELD_RELEVANCE -> SortField.RELEVANCE;
            case SORT_FIELD_POPULARITY -> SortField.POPULARITY;
            case SORT_FIELD_PRICE_ASC -> SortField.PRICE_ASC;
            case SORT_FIELD_PRICE_DESC -> SortField.PRICE_DESC;
            case SORT_FIELD_NEWEST -> SortField.NEWEST;
            case SORT_FIELD_NAME -> SortField.NAME;
            case SORT_FIELD_UNSPECIFIED -> SortField.UNSPECIFIED;
        };
    }

    private SortOrder mapSortOrder(com.kanokna.search.v1.SortOrder sortOrder) {
        if (sortOrder == null) {
            return SortOrder.UNSPECIFIED;
        }
        return switch (sortOrder) {
            case SORT_ORDER_ASC -> SortOrder.ASC;
            case SORT_ORDER_DESC -> SortOrder.DESC;
            case SORT_ORDER_UNSPECIFIED -> SortOrder.UNSPECIFIED;
        };
    }

    private com.kanokna.search.v1.FacetType mapFacetType(FacetType type) {
        if (type == null) {
            return com.kanokna.search.v1.FacetType.FACET_TYPE_UNSPECIFIED;
        }
        return switch (type) {
            case TERMS -> com.kanokna.search.v1.FacetType.FACET_TYPE_TERMS;
            case RANGE -> com.kanokna.search.v1.FacetType.FACET_TYPE_RANGE;
            case BOOLEAN -> com.kanokna.search.v1.FacetType.FACET_TYPE_BOOLEAN;
            case UNSPECIFIED -> com.kanokna.search.v1.FacetType.FACET_TYPE_UNSPECIFIED;
        };
    }

    private com.kanokna.search.v1.ProductStatus mapStatus(ProductStatus status) {
        if (status == null) {
            return com.kanokna.search.v1.ProductStatus.PRODUCT_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case ACTIVE -> com.kanokna.search.v1.ProductStatus.PRODUCT_STATUS_ACTIVE;
            case DRAFT -> com.kanokna.search.v1.ProductStatus.PRODUCT_STATUS_DRAFT;
            case ARCHIVED -> com.kanokna.search.v1.ProductStatus.PRODUCT_STATUS_ARCHIVED;
            case UNSPECIFIED -> com.kanokna.search.v1.ProductStatus.PRODUCT_STATUS_UNSPECIFIED;
        };
    }

    private com.kanokna.search.v1.SuggestionType mapSuggestionType(SuggestionType type) {
        if (type == null) {
            return com.kanokna.search.v1.SuggestionType.SUGGESTION_TYPE_UNSPECIFIED;
        }
        return switch (type) {
            case PRODUCT -> com.kanokna.search.v1.SuggestionType.SUGGESTION_TYPE_PRODUCT;
            case CATEGORY -> com.kanokna.search.v1.SuggestionType.SUGGESTION_TYPE_CATEGORY;
            case BRAND -> com.kanokna.search.v1.SuggestionType.SUGGESTION_TYPE_BRAND;
            case UNSPECIFIED -> com.kanokna.search.v1.SuggestionType.SUGGESTION_TYPE_UNSPECIFIED;
        };
    }

    private com.kanokna.common.v1.Money toMoney(Money money) {
        long minor = money.getAmount()
            .movePointRight(money.getCurrency().getDefaultScale())
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        return com.kanokna.common.v1.Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(mapProtoCurrency(money.getCurrency()))
            .build();
    }

    private com.kanokna.common.v1.Currency mapProtoCurrency(Currency currency) {
        if (currency == null) {
            return com.kanokna.common.v1.Currency.CURRENCY_UNSPECIFIED;
        }
        return switch (currency) {
            case EUR -> com.kanokna.common.v1.Currency.CURRENCY_EUR;
            case USD -> com.kanokna.common.v1.Currency.CURRENCY_USD;
            case RUB -> com.kanokna.common.v1.Currency.CURRENCY_RUB;
        };
    }

    private Currency mapCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must be provided");
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "EUR" -> Currency.EUR;
            case "USD" -> Currency.USD;
            case "RUB" -> Currency.RUB;
            default -> throw new IllegalArgumentException("Unsupported currency: " + currency);
        };
    }

    private Language mapLanguage(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        String normalized = language.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("EN")) {
            return Language.EN;
        }
        if (normalized.startsWith("DE")) {
            return Language.DE;
        }
        if (normalized.startsWith("FR")) {
            return Language.FR;
        }
        if (normalized.startsWith("RU")) {
            return Language.RU;
        }
        return null;
    }

    private String resolveLocalized(LocalizedString value, Language language) {
        if (value == null) {
            return "";
        }
        return value.resolve(language);
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String blankToEmpty(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value;
    }
}
