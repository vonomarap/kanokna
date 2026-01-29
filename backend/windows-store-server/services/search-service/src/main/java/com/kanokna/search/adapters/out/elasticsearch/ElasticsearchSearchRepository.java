package com.kanokna.search.adapters.out.elasticsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kanokna.search.adapters.config.SearchProperties;
import com.kanokna.search.application.dto.BulkIndexResult;
import com.kanokna.search.application.dto.DeleteResult;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.IndexResult;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.domain.exception.SearchDomainErrors;
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
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.CompletionContext;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Context;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggester;

/**
 * Elasticsearch adapter for search queries and indexing operations.
 */
@Component
public class ElasticsearchSearchRepository implements SearchRepository {
    private static final String FIELD_NAME = "name";
    private static final String FIELD_NAME_KEYWORD = "name.keyword";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_FAMILY = "family";
    private static final String FIELD_PROFILE_SYSTEM = "profileSystem";
    private static final String FIELD_OPENING_TYPES = "openingTypes";
    private static final String FIELD_MATERIALS = "materials";
    private static final String FIELD_COLORS = "colors";
    private static final String FIELD_MIN_PRICE = "minPrice";
    private static final String FIELD_MAX_PRICE = "maxPrice";
    private static final String FIELD_CURRENCY = "currency";
    private static final String FIELD_POPULARITY = "popularity";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_PUBLISHED_AT = "publishedAt";
    private static final String FIELD_THUMBNAIL_URL = "thumbnailUrl";
    private static final String FIELD_OPTION_COUNT = "optionCount";
    private static final String FIELD_SUGGEST = "suggest";
    private static final String SUGGESTER_AUTOCOMPLETE = "autocomplete";
    private static final Language DEFAULT_LANGUAGE = Language.RU;

    private static final Set<String> FACET_FIELDS = Set.of(
            FIELD_FAMILY,
            FIELD_PROFILE_SYSTEM,
            FIELD_MATERIALS,
            FIELD_COLORS,
            FIELD_OPENING_TYPES);

    private final ElasticsearchClient client;
    private final SearchProperties searchProperties;

    public ElasticsearchSearchRepository(ElasticsearchClient client, SearchProperties searchProperties) {
        this.client = client;
        this.searchProperties = searchProperties;
        if (this.searchProperties.index() == null || this.searchProperties.index().alias() == null) {
            throw new IllegalStateException("Search index alias must be configured");
        }
    }

    @Override
    public SearchResult search(SearchQuery query) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(indexAlias())
                .from(query.page() * query.pageSize())
                .size(query.pageSize())
                .trackTotalHits(t -> t.enabled(true))
                .query(buildSearchQuery(query))
                .highlight(h -> h
                        .fields(FIELD_NAME, f -> f)
                        .fields(FIELD_DESCRIPTION, f -> f));

        for (String field : FACET_FIELDS) {
            builder.aggregations(field, a -> a.terms(t -> t.field(field).size(50)));
        }

        applySorting(builder, query);

        try {
            SearchResponse<SearchIndexDocument> response = client.search(builder.build(), SearchIndexDocument.class);
            if (response.timedOut()) {
                throw SearchDomainErrors.queryTimeout("Search query timed out");
            }
            return mapSearchResponse(response, query);
        } catch (ElasticsearchException ex) {
            throw mapSearchException(ex, indexAlias());
        } catch (Exception ex) {
            throw SearchDomainErrors.elasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public AutocompleteResult autocomplete(AutocompleteQuery query) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(indexAlias())
                .size(0)
                .suggest(buildAutocompleteSuggester(query));

        try {
            SearchResponse<SearchIndexDocument> response = client.search(builder.build(), SearchIndexDocument.class);
            List<Suggestion> suggestions = mapSuggestions(response);
            return new AutocompleteResult(suggestions, List.of(), Math.toIntExact(response.took()));
        } catch (ElasticsearchException ex) {
            throw SearchDomainErrors.autocompleteElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.autocompleteElasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public FacetValuesResult facetValues(GetFacetValuesQuery query) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(indexAlias())
                .size(0)
                .query(activeOnlyQuery());

        for (String field : query.fields()) {
            builder.aggregations(field, a -> a.terms(t -> t.field(field).size(50)));
        }

        try {
            SearchResponse<SearchIndexDocument> response = client.search(builder.build(), SearchIndexDocument.class);
            List<FacetAggregation> facets = mapFacetAggregations(response.aggregations(), new HashSet<>(query.fields()),
                    Map.of());
            return new FacetValuesResult(facets, Math.toIntExact(response.took()));
        } catch (ElasticsearchException ex) {
            throw SearchDomainErrors.facetElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.facetElasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public ProductSearchDocument getById(String productId) {
        try {
            GetResponse<SearchIndexDocument> response = client.get(g -> g.index(indexAlias()).id(productId),
                    SearchIndexDocument.class);
            if (!response.found()) {
                return null;
            }
            return toDomain(response.source(), DEFAULT_LANGUAGE, Map.of(), null);
        } catch (ElasticsearchException ex) {
            throw SearchDomainErrors.getElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.getElasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public IndexResult index(ProductSearchDocument document) {
        SearchIndexDocument indexDocument = toIndexDocument(document);
        long start = System.currentTimeMillis();
        try {
            IndexResponse response = client.index(i -> i
                    .index(indexAlias())
                    .id(document.getId())
                    .document(indexDocument));
            long took = System.currentTimeMillis() - start;
            boolean success = response.result() == Result.Created || response.result() == Result.Updated;
            return new IndexResult(success, response.id(), took);
        } catch (ElasticsearchException ex) {
            String type = ex.error() != null ? ex.error().type() : null;
            if ("mapper_parsing_exception".equals(type)) {
                throw SearchDomainErrors.indexMappingError(ex.getMessage());
            }
            throw SearchDomainErrors.indexElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.indexElasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public DeleteResult delete(String productId) {
        try {
            DeleteResponse response = client.delete(d -> d.index(indexAlias()).id(productId));
            boolean deleted = response.result() == Result.Deleted || response.result() == Result.NotFound;
            return new DeleteResult(deleted);
        } catch (ElasticsearchException ex) {
            throw SearchDomainErrors.deleteElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.deleteElasticsearchUnavailable(ex.getMessage());
        }
    }

    @Override
    public BulkIndexResult bulkIndex(String indexName, List<ProductSearchDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return new BulkIndexResult(0, 0);
        }
        BulkRequest.Builder builder = new BulkRequest.Builder().index(indexName);
        for (ProductSearchDocument document : documents) {
            SearchIndexDocument indexDocument = toIndexDocument(document);
            builder.operations(op -> op.index(idx -> idx.id(document.getId()).document(indexDocument)));
        }

        try {
            BulkResponse response = client.bulk(builder.build());
            long failed = countFailures(response.items());
            long indexed = response.items().size() - failed;
            return new BulkIndexResult(indexed, failed);
        } catch (ElasticsearchException ex) {
            throw SearchDomainErrors.indexElasticsearchUnavailable(ex.getMessage());
        } catch (Exception ex) {
            throw SearchDomainErrors.indexElasticsearchUnavailable(ex.getMessage());
        }
    }

    private Query buildSearchQuery(SearchQuery query) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (query.queryText() == null || query.queryText().isBlank()) {
            bool.must(m -> m.matchAll(ma -> ma));
        } else {
            bool.must(m -> m.multiMatch(mm -> mm
                    .query(query.queryText())
                    .fields(FIELD_NAME, FIELD_DESCRIPTION)));
        }

        bool.filter(activeOnlyQuery());

        for (FacetFilter filter : query.filters()) {
            if (filter.values().isEmpty()) {
                continue;
            }
            List<FieldValue> values = filter.values().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(FieldValue::of)
                    .toList();
            if (values.isEmpty()) {
                continue;
            }
            bool.filter(f -> f.terms(t -> t.field(filter.field()).terms(tf -> tf.value(values))));
        }

        PriceRange priceRange = query.priceRange();
        if (priceRange != null) {
            Money min = priceRange.minPrice();
            Money max = priceRange.maxPrice();
            Currency currency = resolveCurrency(min, max);
            if (currency != null) {
                bool.filter(f -> f.term(t -> t.field(FIELD_CURRENCY).value(currency.name())));
            }
            if (min != null) {
                double minValue = toMinor(min);
                bool.filter(f -> f.range(r -> r.number(n -> n.field(FIELD_MAX_PRICE).gte(minValue))));
            }
            if (max != null) {
                double maxValue = toMinor(max);
                bool.filter(f -> f.range(r -> r.number(n -> n.field(FIELD_MIN_PRICE).lte(maxValue))));
            }
        }

        return new Query.Builder().bool(bool.build()).build();
    }

    private Query activeOnlyQuery() {
        return new Query.Builder()
                .term(t -> t.field(FIELD_STATUS).value(ProductStatus.ACTIVE.name()))
                .build();
    }

    private void applySorting(SearchRequest.Builder builder, SearchQuery query) {
        SortField sortField = query.sortField() == null ? SortField.UNSPECIFIED : query.sortField();
        SortOrder sortOrder = query.sortOrder() == null ? SortOrder.UNSPECIFIED : query.sortOrder();

        switch (sortField) {
            case POPULARITY ->
                builder.sort(s -> s.field(f -> f.field(FIELD_POPULARITY).order(resolveSortOrder(sortOrder, true))));
            case PRICE_ASC -> builder.sort(s -> s
                    .field(f -> f.field(FIELD_MIN_PRICE).order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)));
            case PRICE_DESC -> builder.sort(s -> s.field(
                    f -> f.field(FIELD_MIN_PRICE).order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));
            case NEWEST ->
                builder.sort(s -> s.field(f -> f.field(FIELD_PUBLISHED_AT).order(resolveSortOrder(sortOrder, true))));
            case NAME ->
                builder.sort(s -> s.field(f -> f.field(FIELD_NAME_KEYWORD).order(resolveSortOrder(sortOrder, false))));
            case RELEVANCE, UNSPECIFIED -> {
                // Default to relevance (_score)
            }
            default -> {
                // Default to relevance (_score)
            }
        }
    }

    private co.elastic.clients.elasticsearch._types.SortOrder resolveSortOrder(SortOrder sortOrder,
            boolean defaultDesc) {
        if (sortOrder == SortOrder.DESC) {
            return co.elastic.clients.elasticsearch._types.SortOrder.Desc;
        }
        if (sortOrder == SortOrder.ASC) {
            return co.elastic.clients.elasticsearch._types.SortOrder.Asc;
        }
        return defaultDesc ? co.elastic.clients.elasticsearch._types.SortOrder.Desc
                : co.elastic.clients.elasticsearch._types.SortOrder.Asc;
    }

    private Suggester buildAutocompleteSuggester(AutocompleteQuery query) {
        return new Suggester.Builder()
                .suggesters(SUGGESTER_AUTOCOMPLETE, field -> field
                        .prefix(query.prefix())
                        .completion(comp -> {
                            comp.field(FIELD_SUGGEST);
                            comp.size(query.limit());
                            comp.skipDuplicates(true);
                            List<CompletionContext> contexts = List.of(resolveFamilyContext(query));
                            comp.contexts("family", contexts);
                            return comp;
                        }))
                .build();
    }

    private CompletionContext resolveFamilyContext(AutocompleteQuery query) {
        String familyFilter = query.familyFilter();
        if (familyFilter != null && !familyFilter.isBlank()) {
            return CompletionContext.of(ctx -> ctx
                    .context(Context.of(ctxBuilder -> ctxBuilder.category(familyFilter))));
        }

        // The completion field is configured with a mandatory "family" context. Use an "empty prefix"
        // context query to match all categories when no explicit filter is provided.
        return CompletionContext.of(ctx -> ctx
                .context(Context.of(ctxBuilder -> ctxBuilder.category("")))
                .prefix(true));
    }

    private List<Suggestion> mapSuggestions(SearchResponse<SearchIndexDocument> response) {
        Map<String, List<co.elastic.clients.elasticsearch.core.search.Suggestion<SearchIndexDocument>>> suggest = response
                .suggest();
        if (suggest == null || suggest.isEmpty()) {
            return List.of();
        }
        List<Suggestion> results = new ArrayList<>();
        List<co.elastic.clients.elasticsearch.core.search.Suggestion<SearchIndexDocument>> entries = suggest
                .get(SUGGESTER_AUTOCOMPLETE);
        if (entries == null) {
            return List.of();
        }
        for (co.elastic.clients.elasticsearch.core.search.Suggestion<SearchIndexDocument> entry : entries) {
            if (!entry.isCompletion()) {
                continue;
            }
            CompletionSuggest<SearchIndexDocument> completion = entry.completion();
            for (CompletionSuggestOption<SearchIndexDocument> option : completion.options()) {
                String productId = option.id();
                if (productId == null && option.source() != null) {
                    productId = option.source().getId();
                }
                results.add(new Suggestion(
                        option.text(),
                        SuggestionType.PRODUCT,
                        productId,
                        0,
                        option.text()));
            }
        }
        return results;
    }

    private SearchResult mapSearchResponse(SearchResponse<SearchIndexDocument> response, SearchQuery query) {
        List<ProductSearchDocument> documents = new ArrayList<>();
        Language language = query.language() == null ? DEFAULT_LANGUAGE : query.language();
        for (var hit : response.hits().hits()) {
            Map<String, String> highlights = mapHighlights(hit);
            Float score = hit.score() == null ? null : hit.score().floatValue();
            documents.add(toDomain(hit.source(), language, highlights, score));
        }

        long totalHits = response.hits().total() == null
                ? response.hits().hits().size()
                : response.hits().total().value();
        int totalPages = query.pageSize() == 0 ? 0 : (int) Math.ceil((double) totalHits / query.pageSize());

        Map<String, Set<String>> selectedValues = extractSelectedValues(query.filters());
        List<FacetAggregation> facets = mapFacetAggregations(response.aggregations(), FACET_FIELDS, selectedValues);

        return new SearchResult(
                documents,
                totalHits,
                query.page(),
                query.pageSize(),
                totalPages,
                facets,
                Math.toIntExact(response.took()));
    }

    private Map<String, String> mapHighlights(Hit<SearchIndexDocument> hit) {
        Map<String, String> highlights = new HashMap<>();
        if (hit.highlight() == null) {
            return highlights;
        }
        for (Map.Entry<String, List<String>> entry : hit.highlight().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                highlights.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return highlights;
    }

    private List<FacetAggregation> mapFacetAggregations(
            Map<String, Aggregate> aggregations,
            Set<String> requestedFields,
            Map<String, Set<String>> selectedValues) {
        List<FacetAggregation> facets = new ArrayList<>();
        if (aggregations == null || aggregations.isEmpty()) {
            return facets;
        }
        for (String field : requestedFields) {
            Aggregate aggregate = aggregations.get(field);
            if (aggregate == null || !aggregate.isSterms()) {
                continue;
            }
            StringTermsAggregate terms = aggregate.sterms();
            List<FacetBucket> buckets = new ArrayList<>();
            for (StringTermsBucket bucket : terms.buckets().array()) {
                String key = bucket.key().stringValue();
                boolean selected = selectedValues.getOrDefault(field, Set.of()).contains(key);
                buckets.add(new FacetBucket(key, key, bucket.docCount(), selected));
            }
            facets.add(new FacetAggregation(field, displayName(field), FacetType.TERMS, buckets));
        }
        return facets;
    }

    private Map<String, Set<String>> extractSelectedValues(List<FacetFilter> filters) {
        Map<String, Set<String>> selected = new HashMap<>();
        for (FacetFilter filter : filters) {
            if (filter.values().isEmpty()) {
                continue;
            }
            Set<String> values = new HashSet<>();
            for (String value : filter.values()) {
                if (value != null && !value.isBlank()) {
                    values.add(value);
                }
            }
            if (!values.isEmpty()) {
                selected.put(filter.field(), values);
            }
        }
        return selected;
    }

    private String displayName(String field) {
        return switch (field) {
            case FIELD_PROFILE_SYSTEM -> "profileSystem";
            case FIELD_OPENING_TYPES -> "openingTypes";
            default -> field;
        };
    }

    private SearchIndexDocument toIndexDocument(ProductSearchDocument document) {
        SearchIndexDocument indexDocument = new SearchIndexDocument();
        indexDocument.setId(document.getId());
        indexDocument.setName(resolveLocalized(document.getName(), DEFAULT_LANGUAGE));
        indexDocument.setDescription(resolveLocalized(document.getDescription(), DEFAULT_LANGUAGE));
        indexDocument.setFamily(document.getFamily());
        indexDocument.setProfileSystem(document.getProfileSystem());
        indexDocument.setOpeningTypes(document.getOpeningTypes());
        indexDocument.setMaterials(document.getMaterials());
        indexDocument.setColors(document.getColors());
        indexDocument.setMinPrice(toMinorLong(document.getMinPrice()));
        indexDocument.setMaxPrice(toMinorLong(document.getMaxPrice()));
        indexDocument.setCurrency(document.getCurrency());
        indexDocument.setPopularity(document.getPopularity());
        indexDocument.setStatus(document.getStatus().name());
        indexDocument.setPublishedAt(document.getPublishedAt());
        indexDocument.setThumbnailUrl(document.getThumbnailUrl());
        indexDocument.setOptionCount(document.getOptionCount());
        if (document.getStatus() == ProductStatus.ACTIVE) {
            indexDocument.setSuggest(document.getSuggestInputs());
        } else {
            indexDocument.setSuggest(List.of());
        }
        return indexDocument;
    }

    private ProductSearchDocument toDomain(
            SearchIndexDocument document,
            Language language,
            Map<String, String> highlights,
            Float score) {
        if (document == null) {
            return null;
        }
        Money minPrice = toMoney(document.getMinPrice(), document.getCurrency());
        Money maxPrice = toMoney(document.getMaxPrice(), document.getCurrency());
        ProductStatus status = parseStatus(document.getStatus());

        return ProductSearchDocument.builder(document.getId())
                .name(localizedString(document.getName(), language))
                .description(localizedString(document.getDescription(), language))
                .family(document.getFamily())
                .profileSystem(document.getProfileSystem())
                .openingTypes(document.getOpeningTypes())
                .materials(document.getMaterials())
                .colors(document.getColors())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .currency(document.getCurrency())
                .popularity(document.getPopularity() == null ? 0 : document.getPopularity())
                .status(status)
                .publishedAt(document.getPublishedAt())
                .thumbnailUrl(document.getThumbnailUrl())
                .optionCount(document.getOptionCount() == null ? 0 : document.getOptionCount())
                .suggestInputs(List.of())
                .score(score)
                .highlights(highlights)
                .build();
    }

    private String resolveLocalized(LocalizedString value, Language language) {
        if (value == null) {
            return null;
        }
        return value.resolve(language == null ? DEFAULT_LANGUAGE : language);
    }

    private LocalizedString localizedString(String value, Language language) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Language resolved = language == null ? DEFAULT_LANGUAGE : language;
        return LocalizedString.of(resolved, value);
    }

    private double toMinor(Money money) {
        return money.getAmount()
                .movePointRight(money.getCurrency().getDefaultScale())
                .doubleValue();
    }

    private Long toMinorLong(Money money) {
        if (money == null) {
            return null;
        }
        return money.getAmount()
                .movePointRight(money.getCurrency().getDefaultScale())
                .longValue();
    }

    private Money toMoney(Long minor, String currencyCode) {
        if (minor == null || currencyCode == null) {
            return null;
        }
        Currency currency = parseCurrency(currencyCode);
        return Money.ofMinor(minor, currency);
    }

    private Currency parseCurrency(String currencyCode) {
        if (currencyCode == null) {
            return Currency.RUB;
        }
        String upper = currencyCode.trim().toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "EUR" -> Currency.EUR;
            case "USD" -> Currency.USD;
            default -> Currency.RUB;
        };
    }

    private Currency resolveCurrency(Money min, Money max) {
        if (min != null) {
            return min.getCurrency();
        }
        if (max != null) {
            return max.getCurrency();
        }
        return null;
    }

    private ProductStatus parseStatus(String status) {
        if (status == null) {
            return ProductStatus.UNSPECIFIED;
        }
        try {
            return ProductStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            return ProductStatus.UNSPECIFIED;
        }
    }

    private RuntimeException mapSearchException(ElasticsearchException ex, String indexName) {
        String type = ex.error() != null ? ex.error().type() : null;
        if ("index_not_found_exception".equals(type)) {
            return SearchDomainErrors.indexNotFound(indexName);
        }
        if ("search_phase_execution_exception".equals(type) && ex.getMessage() != null
                && ex.getMessage().toLowerCase(Locale.ROOT).contains("timeout")) {
            return SearchDomainErrors.queryTimeout(ex.getMessage());
        }
        return SearchDomainErrors.elasticsearchUnavailable(ex.getMessage());
    }

    private long countFailures(List<BulkResponseItem> items) {
        long failed = 0;
        for (BulkResponseItem item : items) {
            if (item.error() != null) {
                failed++;
            }
        }
        return failed;
    }

    private String indexAlias() {
        return searchProperties.index().alias();
    }
}
