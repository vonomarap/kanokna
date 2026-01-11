package com.kanokna.search.application.service;

import com.kanokna.search.adapters.config.SearchProperties;
import com.kanokna.search.application.dto.BulkIndexResult;
import com.kanokna.search.application.dto.CatalogProductDeleteEvent;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.dto.CatalogProductPage;
import com.kanokna.search.application.dto.DeleteResult;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.GetProductByIdQuery;
import com.kanokna.search.application.dto.IndexResult;
import com.kanokna.search.application.dto.ReindexCommand;
import com.kanokna.search.application.dto.ReindexResult;
import com.kanokna.search.application.port.in.AutocompleteUseCase;
import com.kanokna.search.application.port.in.DeleteProductUseCase;
import com.kanokna.search.application.port.in.GetFacetValuesUseCase;
import com.kanokna.search.application.port.in.GetProductByIdUseCase;
import com.kanokna.search.application.port.in.IndexProductUseCase;
import com.kanokna.search.application.port.in.ReindexCatalogUseCase;
import com.kanokna.search.application.port.in.SearchProductsUseCase;
import com.kanokna.search.application.port.out.CatalogConfigurationPort;
import com.kanokna.search.application.port.out.DistributedLockPort;
import com.kanokna.search.application.port.out.SearchIndexAdminPort;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.domain.exception.SearchDomainErrors;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.FacetFilter;
import com.kanokna.search.domain.model.PriceRange;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Application service implementing search use cases.
 */
@Service
@Transactional
public class SearchApplicationService implements
    SearchProductsUseCase,
    AutocompleteUseCase,
    IndexProductUseCase,
    DeleteProductUseCase,
    GetFacetValuesUseCase,
    GetProductByIdUseCase,
    ReindexCatalogUseCase {

    private static final Logger log = LoggerFactory.getLogger(SearchApplicationService.class);
    private static final String SERVICE = "search-service";
    private static final String USE_CASE_BROWSE = "UC-CATALOG-BROWSE";
    private static final String USE_CASE_INDEX = "INDEX-PRODUCT";
    private static final String USE_CASE_DELETE = "DELETE-PRODUCT";
    private static final String USE_CASE_REINDEX = "ADMIN-REINDEX";
    private static final int MIN_AUTOCOMPLETE_PREFIX = 2;
    private static final int DEFAULT_AUTOCOMPLETE_LIMIT = 10;
    private static final int MAX_AUTOCOMPLETE_LIMIT = 20;
    private static final Language DEFAULT_LANGUAGE = Language.RU;
    private static final Set<String> VALID_FACET_FIELDS = Set.of(
        "family",
        "profileSystem",
        "materials",
        "colors",
        "openingTypes"
    );

    private final SearchRepository searchRepository;
    private final SearchIndexAdminPort searchIndexAdminPort;
    private final CatalogConfigurationPort catalogConfigurationPort;
    private final DistributedLockPort distributedLockPort;
    private final SearchProperties searchProperties;

    public SearchApplicationService(
        SearchRepository searchRepository,
        SearchIndexAdminPort searchIndexAdminPort,
        CatalogConfigurationPort catalogConfigurationPort,
        DistributedLockPort distributedLockPort,
        SearchProperties searchProperties
    ) {
        this.searchRepository = searchRepository;
        this.searchIndexAdminPort = searchIndexAdminPort;
        this.catalogConfigurationPort = catalogConfigurationPort;
        this.distributedLockPort = distributedLockPort;
        this.searchProperties = searchProperties;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-searchProducts"
        LAYER="application.service"
        INTENT="Execute full-text search with faceted filtering and return paginated results"
        INPUT="SearchQuery"
        OUTPUT="SearchResult"
        SIDE_EFFECTS="None (read-only)"
        LINKS="RequirementsAnalysis.xml#UC-CATALOG-BROWSE;RequirementsAnalysis.xml#NFR-PERF-SEARCH-LATENCY">

        <PRECONDITIONS>
            <Item>Elasticsearch cluster is available and healthy</Item>
            <Item>product_templates index exists (or alias points to valid index)</Item>
            <Item>pageSize is between 1 and 100 (validated in adapter)</Item>
            <Item>page is non-negative (validated in adapter)</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>SearchResult contains only products with status=ACTIVE</Item>
            <Item>SearchResult.totalCount reflects total matching documents</Item>
            <Item>SearchResult.products.size() в‰¤ pageSize</Item>
            <Item>SearchResult.facets reflect counts for current filter state</Item>
            <Item>SearchResult.queryTimeMs contains actual execution time</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Query is read-only; does not modify any data</Item>
            <Item>Results are deterministic for same query and index state</Item>
            <Item>Facet counts are computed on filtered result set</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-SEARCH-ES-UNAVAILABLE">Elasticsearch cluster
                unreachable</Item>
            <Item type="TECHNICAL" code="ERR-SEARCH-INDEX-NOT-FOUND">product_templates index does
                not exist</Item>
            <Item type="TECHNICAL" code="ERR-SEARCH-QUERY-TIMEOUT">Query exceeded timeout threshold</Item>
            <Item type="BUSINESS" code="ERR-SEARCH-INVALID-FACET">Unknown facet field specified in
                filter</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-SEARCH-QUERY-01">Build Elasticsearch query from SearchQuery</Item>
            <Item id="BA-SEARCH-QUERY-02">Execute query and collect results</Item>
            <Item id="BA-SEARCH-QUERY-03">Process facet aggregations</Item>
            <Item id="BA-SEARCH-QUERY-04">Map Elasticsearch response to SearchResult</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-SEARCH-QUERY-01][STATE=BUILD_QUERY]
                eventType=QUERY_BUILDING keyValues=queryText,filterCount,sortField</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-SEARCH-QUERY-02][STATE=EXECUTE_QUERY]
                eventType=ELASTICSEARCH_QUERY_START keyValues=indexName</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-SEARCH-QUERY-02][STATE=QUERY_COMPLETE]
                eventType=ELASTICSEARCH_QUERY_END decision=SUCCESS|FAILURE
                keyValues=took_ms,total_hits</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-SEARCH-QUERY-04][STATE=MAPPING_COMPLETE]
                eventType=SEARCH_RESULT_READY keyValues=resultCount,facetCount</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-SEARCH-001">Empty query returns all active products</Case>
            <Case id="TC-FUNC-SEARCH-002">Text query matches name and description fields</Case>
            <Case id="TC-FUNC-SEARCH-003">Family filter returns only matching family</Case>
            <Case id="TC-FUNC-SEARCH-004">Price range filter works correctly</Case>
            <Case id="TC-FUNC-SEARCH-005">Multiple filters combine with AND logic</Case>
            <Case id="TC-FUNC-SEARCH-006">Sorting by relevance returns highest scores first</Case>
            <Case id="TC-FUNC-SEARCH-007">Pagination returns correct slice of results</Case>
            <Case id="TC-FUNC-SEARCH-008">Facet counts update based on active filters</Case>
            <Case id="TC-FUNC-SEARCH-009">Elasticsearch failure returns technical error</Case>
            <Case id="TC-FUNC-SEARCH-010">Query time is recorded in result</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    @Transactional(readOnly = true)
    public SearchResult searchProducts(SearchQuery query) {
        Objects.requireNonNull(query, "query");

        SearchQuery normalized = normalizeSearchQuery(query);
        validateFacetFilters(normalized.filters());

        // BA-SEARCH-QUERY-01: Build Elasticsearch query from SearchQuery
        log.info(logLine(USE_CASE_BROWSE, "BA-SEARCH-QUERY-01", "BUILD_QUERY",
            "QUERY_BUILDING", "NORMALIZE",
            "queryText=" + safeValue(normalized.queryText())
                + ",filterCount=" + normalized.filters().size()
                + ",sortField=" + normalized.sortField()));

        // BA-SEARCH-QUERY-02: Execute query and collect results
        log.info(logLine(USE_CASE_BROWSE, "BA-SEARCH-QUERY-02", "EXECUTE_QUERY",
            "ELASTICSEARCH_QUERY_START", "START",
            "indexName=" + searchProperties.getIndex().getAlias()));

        SearchResult result = searchRepository.search(normalized);

        log.info(logLine(USE_CASE_BROWSE, "BA-SEARCH-QUERY-02", "QUERY_COMPLETE",
            "ELASTICSEARCH_QUERY_END", "SUCCESS",
            "took_ms=" + result.queryTimeMs() + ",total_hits=" + result.totalCount()));

        // BA-SEARCH-QUERY-03: Process facet aggregations
        log.info(logLine(USE_CASE_BROWSE, "BA-SEARCH-QUERY-03", "PROCESS_FACETS",
            "FACET_AGGREGATION", "COLLECT",
            "facetCount=" + result.facets().size()));

        // BA-SEARCH-QUERY-04: Map Elasticsearch response to SearchResult
        log.info(logLine(USE_CASE_BROWSE, "BA-SEARCH-QUERY-04", "MAPPING_COMPLETE",
            "SEARCH_RESULT_READY", "READY",
            "resultCount=" + result.products().size()
                + ",facetCount=" + result.facets().size()));

        return result;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-autocomplete"
        LAYER="application.service"
        INTENT="Provide autocomplete suggestions as user types product search query"
        INPUT="AutocompleteQuery (prefix, limit, language, familyFilter)"
        OUTPUT="AutocompleteResult (suggestions, categoryHints)"
        SIDE_EFFECTS="None (read-only)"
        LINKS="RequirementsAnalysis.xml#UC-CATALOG-BROWSE">

        <PRECONDITIONS>
            <Item>prefix.length() >= 2 (minimum characters for suggestions)</Item>
            <Item>limit is between 1 and 20</Item>
            <Item>Elasticsearch cluster is available</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>AutocompleteResult.suggestions.size() в‰¤ limit</Item>
            <Item>Suggestions are ordered by relevance/popularity</Item>
            <Item>Only suggestions from ACTIVE products included</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Query is read-only</Item>
            <Item>Suggestions are case-insensitive prefix matches</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-AUTO-PREFIX-TOO-SHORT">Prefix has fewer than 2
                characters</Item>
            <Item type="TECHNICAL" code="ERR-AUTO-ES-UNAVAILABLE">Elasticsearch unreachable</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-AUTO-01">Validate prefix length constraint</Item>
            <Item id="BA-AUTO-02">Build completion suggester query</Item>
            <Item id="BA-AUTO-03">Execute suggestion query</Item>
            <Item id="BA-AUTO-04">Map suggestions to result</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-AUTO-01][STATE=VALIDATE_PREFIX]
                eventType=AUTOCOMPLETE_REQUEST keyValues=prefix,prefixLength</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-AUTO-03][STATE=EXECUTE_SUGGEST]
                eventType=SUGGEST_QUERY_START keyValues=limit</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-AUTO-04][STATE=RESULT_READY]
                eventType=AUTOCOMPLETE_COMPLETE keyValues=suggestionCount,queryTimeMs</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-AUTO-001">Valid prefix returns relevant suggestions</Case>
            <Case id="TC-FUNC-AUTO-002">Prefix less than 2 chars returns empty or error</Case>
            <Case id="TC-FUNC-AUTO-003">Family filter restricts suggestions</Case>
            <Case id="TC-FUNC-AUTO-004">Limit parameter controls max suggestions</Case>
            <Case id="TC-FUNC-AUTO-005">Archived products excluded from suggestions</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    @Transactional(readOnly = true)
    public AutocompleteResult autocomplete(AutocompleteQuery query) {
        Objects.requireNonNull(query, "query");

        AutocompleteQuery normalized = normalizeAutocompleteQuery(query);
        String prefix = normalized.prefix() == null ? "" : normalized.prefix();
        int prefixLength = prefix.length();

        // BA-AUTO-01: Validate prefix length constraint
        log.info(logLine(USE_CASE_BROWSE, "BA-AUTO-01", "VALIDATE_PREFIX",
            "AUTOCOMPLETE_REQUEST", "CHECK",
            "prefix=" + safeValue(prefix) + ",prefixLength=" + prefixLength));

        if (prefixLength < MIN_AUTOCOMPLETE_PREFIX) {
            throw SearchDomainErrors.autocompletePrefixTooShort(prefix);
        }

        // BA-AUTO-02: Build completion suggester query

        // BA-AUTO-03: Execute suggestion query
        log.info(logLine(USE_CASE_BROWSE, "BA-AUTO-03", "EXECUTE_SUGGEST",
            "SUGGEST_QUERY_START", "START",
            "limit=" + normalized.limit()));

        AutocompleteResult result = searchRepository.autocomplete(normalized);

        // BA-AUTO-04: Map suggestions to result
        log.info(logLine(USE_CASE_BROWSE, "BA-AUTO-04", "RESULT_READY",
            "AUTOCOMPLETE_COMPLETE", "SUCCESS",
            "suggestionCount=" + result.suggestions().size()
                + ",queryTimeMs=" + result.queryTimeMs()));

        return result;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-indexProduct"
        LAYER="application.service"
        INTENT="Index or update a product document in Elasticsearch when catalog event is received"
        INPUT="ProductTemplatePublishedEvent OR ProductTemplateUpdatedEvent (from Kafka)"
        OUTPUT="IndexResult (success/failure, documentId)"
        SIDE_EFFECTS="Creates or updates document in Elasticsearch index"
        LINKS="DevelopmentPlan.xml#Flow-Event-Driven">

        <PRECONDITIONS>
            <Item>Event (Published or Updated) is valid and deserialized</Item>
            <Item>Event contains required fields: product_template_id, name, product_family, all
                search fields</Item>
            <Item>Elasticsearch cluster is available</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>Document with productId exists in product_templates index</Item>
            <Item>Document fields match event payload</Item>
            <Item>Suggest field populated for autocomplete</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Indexing is idempotent: same event reprocessed produces same document</Item>
            <Item>Document ID equals product_template_id</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-INDEX-ES-UNAVAILABLE">Elasticsearch cluster unreachable</Item>
            <Item type="TECHNICAL" code="ERR-INDEX-MAPPING-ERROR">Document does not match index
                mapping</Item>
            <Item type="BUSINESS" code="ERR-INDEX-INVALID-EVENT">Event missing required fields</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-INDEX-01">Validate and extract event payload</Item>
            <Item id="BA-INDEX-02">Transform event to ProductSearchDocument</Item>
            <Item id="BA-INDEX-03">Execute index/update operation</Item>
            <Item id="BA-INDEX-04">Confirm indexing success</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=INDEX-PRODUCT][BLOCK=BA-INDEX-01][STATE=EVENT_RECEIVED]
                eventType=CATALOG_EVENT_RECEIVED keyValues=eventId,productId,eventType</Item>
            <Item>[SVC=search-service][UC=INDEX-PRODUCT][BLOCK=BA-INDEX-02][STATE=TRANSFORM]
                eventType=DOCUMENT_CREATED keyValues=productId</Item>
            <Item>[SVC=search-service][UC=INDEX-PRODUCT][BLOCK=BA-INDEX-03][STATE=INDEX_EXECUTE]
                eventType=ES_INDEX_REQUEST keyValues=productId,action=UPSERT</Item>
            <Item>[SVC=search-service][UC=INDEX-PRODUCT][BLOCK=BA-INDEX-04][STATE=INDEX_COMPLETE]
                eventType=PRODUCT_INDEXED decision=SUCCESS|FAILURE keyValues=productId,took_ms</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-INDEX-001">Published event creates new document</Case>
            <Case id="TC-FUNC-INDEX-002">Updated event updates existing document (uses extended
                payload v1.2.0)</Case>
            <Case id="TC-FUNC-INDEX-003">Same event reprocessed is idempotent</Case>
            <Case id="TC-FUNC-INDEX-004">Invalid event logged and skipped</Case>
            <Case id="TC-FUNC-INDEX-005">Elasticsearch failure triggers retry</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    public IndexResult indexProduct(CatalogProductEvent event) {
        Objects.requireNonNull(event, "event");

        String productId = safeValue(event.productId());
        String eventId = safeValue(event.eventId());
        String eventType = safeValue(event.eventType());

        // BA-INDEX-01: Validate and extract event payload
        log.info(logLine(USE_CASE_INDEX, "BA-INDEX-01", "EVENT_RECEIVED",
            "CATALOG_EVENT_RECEIVED", "RECEIVED",
            "eventId=" + eventId + ",productId=" + productId + ",eventType=" + eventType));

        validateCatalogEvent(event);

        // BA-INDEX-02: Transform event to ProductSearchDocument
        ProductSearchDocument document = toDocument(event);
        log.info(logLine(USE_CASE_INDEX, "BA-INDEX-02", "TRANSFORM",
            "DOCUMENT_CREATED", "READY",
            "productId=" + productId));

        // BA-INDEX-03: Execute index/update operation
        log.info(logLine(USE_CASE_INDEX, "BA-INDEX-03", "INDEX_EXECUTE",
            "ES_INDEX_REQUEST", "UPSERT",
            "productId=" + productId + ",action=UPSERT"));

        IndexResult result = searchRepository.index(document);

        // BA-INDEX-04: Confirm indexing success
        log.info(logLine(USE_CASE_INDEX, "BA-INDEX-04", "INDEX_COMPLETE",
            "PRODUCT_INDEXED", result.success() ? "SUCCESS" : "FAILURE",
            "productId=" + productId + ",took_ms=" + result.tookMs()));

        return result;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-deleteProduct"
        LAYER="application.service"
        INTENT="Remove a product document from Elasticsearch when product is unpublished/archived"
        INPUT="ProductTemplateUnpublishedEvent (from Kafka) or productId"
        OUTPUT="DeleteResult (success/notFound)"
        SIDE_EFFECTS="Deletes document from Elasticsearch index"
        LINKS="DevelopmentPlan.xml#Flow-Event-Driven">

        <PRECONDITIONS>
            <Item>productId is provided and non-empty</Item>
            <Item>Elasticsearch cluster is available</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>Document with productId no longer exists in index, OR was already absent</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Delete is idempotent: deleting non-existent document succeeds</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-DELETE-ES-UNAVAILABLE">Elasticsearch cluster
                unreachable</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-DELETE-01">Extract productId from event</Item>
            <Item id="BA-DELETE-02">Execute delete operation</Item>
            <Item id="BA-DELETE-03">Log outcome</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=DELETE-PRODUCT][BLOCK=BA-DELETE-01][STATE=EVENT_RECEIVED]
                eventType=UNPUBLISH_EVENT_RECEIVED keyValues=productId</Item>
            <Item>[SVC=search-service][UC=DELETE-PRODUCT][BLOCK=BA-DELETE-02][STATE=DELETE_EXECUTE]
                eventType=ES_DELETE_REQUEST keyValues=productId</Item>
            <Item>[SVC=search-service][UC=DELETE-PRODUCT][BLOCK=BA-DELETE-03][STATE=DELETE_COMPLETE]
                eventType=PRODUCT_DELETED decision=DELETED|NOT_FOUND keyValues=productId</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-DELETE-001">Unpublished event removes document</Case>
            <Case id="TC-FUNC-DELETE-002">Delete non-existent document succeeds (idempotent)</Case>
            <Case id="TC-FUNC-DELETE-003">Elasticsearch failure triggers retry</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    public DeleteResult deleteProduct(CatalogProductDeleteEvent event) {
        Objects.requireNonNull(event, "event");
        String productId = safeValue(event.productId());

        // BA-DELETE-01: Extract productId from event
        log.info(logLine(USE_CASE_DELETE, "BA-DELETE-01", "EVENT_RECEIVED",
            "UNPUBLISH_EVENT_RECEIVED", "RECEIVED",
            "productId=" + productId));

        if (productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }

        // BA-DELETE-02: Execute delete operation
        log.info(logLine(USE_CASE_DELETE, "BA-DELETE-02", "DELETE_EXECUTE",
            "ES_DELETE_REQUEST", "DELETE",
            "productId=" + productId));

        DeleteResult result = searchRepository.delete(productId);

        // BA-DELETE-03: Log outcome
        log.info(logLine(USE_CASE_DELETE, "BA-DELETE-03", "DELETE_COMPLETE",
            "PRODUCT_DELETED", result.deleted() ? "DELETED" : "NOT_FOUND",
            "productId=" + productId));

        return result;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-getFacetValues"
        LAYER="application.service"
        INTENT="Retrieve available facet values for filtering UI dropdowns"
        INPUT="GetFacetValuesQuery (fields, language)"
        OUTPUT="FacetValuesResult (facets with counts)"
        SIDE_EFFECTS="None (read-only)"
        LINKS="RequirementsAnalysis.xml#UC-CATALOG-BROWSE">

        <PRECONDITIONS>
            <Item>Elasticsearch cluster is available</Item>
            <Item>At least one facet field is specified</Item>
            <Item>Requested fields are valid facet fields (family, profileSystem, materials, colors,
                openingTypes)</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>FacetValuesResult contains requested facet fields</Item>
            <Item>Each facet includes all distinct values with document counts</Item>
            <Item>Only values from ACTIVE products are included</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Query is read-only</Item>
            <Item>Facet values are sorted by count (descending) or alphabetically</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-FACET-ES-UNAVAILABLE">Elasticsearch cluster unreachable</Item>
            <Item type="BUSINESS" code="ERR-FACET-INVALID-FIELD">Unknown facet field requested</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-FACET-01">Validate requested facet fields</Item>
            <Item id="BA-FACET-02">Build aggregation query</Item>
            <Item id="BA-FACET-03">Execute aggregation query</Item>
            <Item id="BA-FACET-04">Map aggregations to FacetValuesResult</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-FACET-01][STATE=VALIDATE_FIELDS]
                eventType=FACET_REQUEST keyValues=fieldCount,fields</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-FACET-03][STATE=EXECUTE_AGGREGATION]
                eventType=ES_AGGREGATION_START keyValues=fieldCount</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-FACET-04][STATE=RESULT_READY]
                eventType=FACET_VALUES_READY keyValues=facetCount,queryTimeMs</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-FACET-001">Request single facet field returns values</Case>
            <Case id="TC-FUNC-FACET-002">Request multiple facet fields returns all</Case>
            <Case id="TC-FUNC-FACET-003">Invalid field name returns error</Case>
            <Case id="TC-FUNC-FACET-004">Archived products excluded from counts</Case>
            <Case id="TC-FUNC-FACET-005">Empty index returns empty facets</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    @Transactional(readOnly = true)
    public FacetValuesResult getFacetValues(GetFacetValuesQuery query) {
        Objects.requireNonNull(query, "query");

        GetFacetValuesQuery normalized = normalizeFacetValuesQuery(query);
        validateFacetValueFields(normalized.fields());

        // BA-FACET-01: Validate requested facet fields
        log.info(logLine(USE_CASE_BROWSE, "BA-FACET-01", "VALIDATE_FIELDS",
            "FACET_REQUEST", "VALIDATE",
            "fieldCount=" + normalized.fields().size() + ",fields=" + normalized.fields()));

        // BA-FACET-02: Build aggregation query

        // BA-FACET-03: Execute aggregation query
        log.info(logLine(USE_CASE_BROWSE, "BA-FACET-03", "EXECUTE_AGGREGATION",
            "ES_AGGREGATION_START", "START",
            "fieldCount=" + normalized.fields().size()));

        FacetValuesResult result = searchRepository.facetValues(normalized);

        // BA-FACET-04: Map aggregations to FacetValuesResult
        log.info(logLine(USE_CASE_BROWSE, "BA-FACET-04", "RESULT_READY",
            "FACET_VALUES_READY", "SUCCESS",
            "facetCount=" + result.facets().size()
                + ",queryTimeMs=" + result.queryTimeMs()));

        return result;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-getProductById"
        LAYER="application.service"
        INTENT="Retrieve a single product document from the search index by ID"
        INPUT="GetProductByIdQuery (productId, language)"
        OUTPUT="ProductSearchDocument or NOT_FOUND"
        SIDE_EFFECTS="None (read-only)"
        LINKS="RequirementsAnalysis.xml#UC-CATALOG-BROWSE">

        <PRECONDITIONS>
            <Item>productId is provided and non-empty</Item>
            <Item>Elasticsearch cluster is available</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>If document exists, return ProductSearchDocument with all fields</Item>
            <Item>If document does not exist, return NOT_FOUND result</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Query is read-only</Item>
            <Item>Returns document regardless of status (ACTIVE/DRAFT/ARCHIVED)</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-GET-ES-UNAVAILABLE">Elasticsearch cluster unreachable</Item>
            <Item type="BUSINESS" code="ERR-GET-PRODUCT-NOT-FOUND">Product not found in index</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-GET-01">Validate productId</Item>
            <Item id="BA-GET-02">Execute get-by-id query</Item>
            <Item id="BA-GET-03">Map document to ProductSearchDocument</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-GET-01][STATE=VALIDATE_ID]
                eventType=GET_PRODUCT_REQUEST keyValues=productId</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-GET-02][STATE=EXECUTE_GET]
                eventType=ES_GET_REQUEST keyValues=productId</Item>
            <Item>[SVC=search-service][UC=UC-CATALOG-BROWSE][BLOCK=BA-GET-03][STATE=RESULT_READY]
                eventType=GET_PRODUCT_COMPLETE decision=FOUND|NOT_FOUND keyValues=productId</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-GET-001">Existing product ID returns document</Case>
            <Case id="TC-FUNC-GET-002">Non-existent product ID returns NOT_FOUND</Case>
            <Case id="TC-FUNC-GET-003">Empty product ID returns validation error</Case>
            <Case id="TC-FUNC-GET-004">Archived product is still retrievable by ID</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    @Transactional(readOnly = true)
    public ProductSearchDocument getProductById(GetProductByIdQuery query) {
        Objects.requireNonNull(query, "query");
        String productId = query.productId();

        // BA-GET-01: Validate productId
        log.info(logLine(USE_CASE_BROWSE, "BA-GET-01", "VALIDATE_ID",
            "GET_PRODUCT_REQUEST", "VALIDATE",
            "productId=" + safeValue(productId)));

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }

        // BA-GET-02: Execute get-by-id query
        log.info(logLine(USE_CASE_BROWSE, "BA-GET-02", "EXECUTE_GET",
            "ES_GET_REQUEST", "START",
            "productId=" + productId));

        ProductSearchDocument document = searchRepository.getById(productId);

        // BA-GET-03: Map document to ProductSearchDocument
        if (document == null) {
            log.info(logLine(USE_CASE_BROWSE, "BA-GET-03", "RESULT_READY",
                "GET_PRODUCT_COMPLETE", "NOT_FOUND",
                "productId=" + productId));
            throw SearchDomainErrors.productNotFound(productId);
        }

        log.info(logLine(USE_CASE_BROWSE, "BA-GET-03", "RESULT_READY",
            "GET_PRODUCT_COMPLETE", "FOUND",
            "productId=" + productId));

        return document;
    }
/*     <FUNCTION_CONTRACT
        id="FC-search-reindexCatalog"
        LAYER="application.service"
        INTENT="Perform full reindex of product catalog with zero-downtime alias swap"
        INPUT="ReindexCommand (optional: sourceIndex)"
        OUTPUT="ReindexResult (newIndexName, documentCount, durationMs)"
        SIDE_EFFECTS="Creates new index, populates from catalog-service, swaps alias"
        LINKS="DevelopmentPlan.xml#Flow-Event-Driven;Technology.xml#TECH-elasticsearch;Technology.xml#DEC-DISTRIBUTED-LOCK"
        SECURITY="Requires ADMIN role">

        <DEPENDENCIES note="Added per ISS-SEARCH-BLUEPRINT-002/003">
            <Item type="infrastructure">TECH-redis: Distributed lock via Redisson
                (DEC-DISTRIBUTED-LOCK)</Item>
            <Item type="grpc">catalog-configuration-service: ListProductTemplates RPC
                (ProductTemplate v1.2.0 with search fields)</Item>
            <Item type="infrastructure">TECH-elasticsearch: Bulk indexing, alias swap</Item>
        </DEPENDENCIES>

        <PRECONDITIONS>
            <Item>Caller has ADMIN role</Item>
            <Item>Elasticsearch cluster is available and healthy</Item>
            <Item>catalog-configuration-service is reachable (for bulk fetch via
                ListProductTemplates)</Item>
            <Item>Redis is available for distributed lock acquisition</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>New versioned index created (e.g., product_templates_v2)</Item>
            <Item>All active products indexed in new index</Item>
            <Item>Alias product_templates now points to new index</Item>
            <Item>Previous index retained for rollback (until manually deleted)</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Alias swap is atomic (no downtime)</Item>
            <Item>Previous index remains valid until explicit cleanup</Item>
            <Item>Only one reindex operation can run at a time (Redis distributed lock)</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="TECHNICAL" code="ERR-REINDEX-ES-UNAVAILABLE">Elasticsearch cluster
                unreachable</Item>
            <Item type="TECHNICAL" code="ERR-REINDEX-CATALOG-UNAVAILABLE">catalog-configuration-service
                unreachable</Item>
            <Item type="TECHNICAL" code="ERR-REINDEX-IN-PROGRESS">Another reindex operation is
                already running (lock held)</Item>
            <Item type="TECHNICAL" code="ERR-REINDEX-ALIAS-SWAP-FAILED">Alias swap operation failed</Item>
            <Item type="TECHNICAL" code="ERR-REINDEX-LOCK-UNAVAILABLE">Redis unavailable for lock
                acquisition</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-REINDEX-01">Acquire distributed lock for reindex (Redis via Redisson)</Item>
            <Item id="BA-REINDEX-02">Create new versioned index with mapping</Item>
            <Item id="BA-REINDEX-03">Fetch all products from catalog-configuration-service
                (ListProductTemplates gRPC)</Item>
            <Item id="BA-REINDEX-04">Bulk index products into new index</Item>
            <Item id="BA-REINDEX-05">Swap alias to new index (atomic)</Item>
            <Item id="BA-REINDEX-06">Release lock and log completion</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-01][STATE=ACQUIRE_LOCK]
                eventType=REINDEX_STARTED keyValues=requestedBy</Item>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-02][STATE=CREATE_INDEX]
                eventType=NEW_INDEX_CREATED keyValues=indexName</Item>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-03][STATE=FETCH_PRODUCTS]
                eventType=CATALOG_FETCH_START keyValues=batchSize</Item>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-04][STATE=BULK_INDEX]
                eventType=BULK_INDEX_PROGRESS keyValues=indexedCount,failedCount</Item>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-05][STATE=SWAP_ALIAS]
                eventType=ALIAS_SWAP keyValues=oldIndex,newIndex</Item>
            <Item>[SVC=search-service][UC=ADMIN-REINDEX][BLOCK=BA-REINDEX-06][STATE=COMPLETE]
                eventType=REINDEX_COMPLETE decision=SUCCESS|FAILURE
                keyValues=documentCount,durationMs</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-REINDEX-001">Successful reindex creates new index and swaps alias</Case>
            <Case id="TC-FUNC-REINDEX-002">Reindex populates all active products from catalog gRPC</Case>
            <Case id="TC-FUNC-REINDEX-003">Concurrent reindex requests are rejected (lock
                contention)</Case>
            <Case id="TC-FUNC-REINDEX-004">Failed reindex does not swap alias</Case>
            <Case id="TC-FUNC-REINDEX-005">Previous index remains after successful swap</Case>
            <Case id="TC-FUNC-REINDEX-006">Non-admin user is rejected</Case>
            <Case id="TC-FUNC-REINDEX-007">Redis unavailable returns ERR-REINDEX-LOCK-UNAVAILABLE</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

    @Override
    public ReindexResult reindexCatalog(ReindexCommand command) {
        Objects.requireNonNull(command, "command");

        String requestedBy = "admin";
        String lockName = searchProperties.getReindex().getLockName();
        long start = System.currentTimeMillis();

        // BA-REINDEX-01: Acquire distributed lock for reindex (Redis via Redisson)
        log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-01", "ACQUIRE_LOCK",
            "REINDEX_STARTED", "REQUEST",
            "requestedBy=" + requestedBy));

        DistributedLockPort.LockHandle lock;
        try {
            lock = distributedLockPort.tryAcquire(lockName);
        } catch (Exception ex) {
            throw SearchDomainErrors.reindexLockUnavailable(ex.getMessage());
        }

        if (lock == null) {
            throw SearchDomainErrors.reindexInProgress();
        }

        try (lock) {
            String alias = searchProperties.getIndex().getAlias();
            String versionPrefix = searchProperties.getIndex().getVersionPrefix();
            List<String> existing;

            try {
                existing = searchIndexAdminPort.resolveAlias(alias);
            } catch (Exception ex) {
                throw SearchDomainErrors.reindexElasticsearchUnavailable(ex.getMessage());
            }

            String newIndexName = nextIndexName(existing, versionPrefix);

            // BA-REINDEX-02: Create new versioned index with mapping
            try {
                searchIndexAdminPort.createIndex(newIndexName);
            } catch (Exception ex) {
                throw SearchDomainErrors.reindexElasticsearchUnavailable(ex.getMessage());
            }

            log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-02", "CREATE_INDEX",
                "NEW_INDEX_CREATED", "SUCCESS",
                "indexName=" + newIndexName));

            int batchSize = searchProperties.getReindex().getBatchSize();
            String pageToken = null;
            long indexedCount = 0;
            long failedCount = 0;

            // BA-REINDEX-03: Fetch all products from catalog-configuration-service (ListProductTemplates gRPC)
            log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-03", "FETCH_PRODUCTS",
                "CATALOG_FETCH_START", "START",
                "batchSize=" + batchSize));

            do {
                CatalogProductPage page;
                try {
                    page = catalogConfigurationPort.listProductTemplates(batchSize, pageToken);
                } catch (Exception ex) {
                    throw SearchDomainErrors.reindexCatalogUnavailable(ex.getMessage());
                }

                List<ProductSearchDocument> documents = new ArrayList<>();
                for (CatalogProductEvent event : page.products()) {
                    if (event == null) {
                        continue;
                    }
                    try {
                        validateCatalogEvent(event);
                    } catch (DomainException ex) {
                        if ("ERR-INDEX-INVALID-EVENT".equals(ex.getCode())) {
                            continue;
                        }
                        throw ex;
                    }
                    documents.add(toDocument(event));
                }

                if (!documents.isEmpty()) {
                    BulkIndexResult bulk;
                    try {
                        bulk = searchRepository.bulkIndex(newIndexName, documents);
                    } catch (DomainException ex) {
                        throw SearchDomainErrors.reindexElasticsearchUnavailable(ex.getMessage());
                    }
                    indexedCount += bulk.indexedCount();
                    failedCount += bulk.failedCount();

                    // BA-REINDEX-04: Bulk index products into new index
                    log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-04", "BULK_INDEX",
                        "BULK_INDEX_PROGRESS", "PROGRESS",
                        "indexedCount=" + indexedCount + ",failedCount=" + failedCount));

                    if (bulk.failedCount() > 0) {
                        throw SearchDomainErrors.reindexElasticsearchUnavailable(
                            "Bulk index failures: " + bulk.failedCount());
                    }
                }

                pageToken = page.nextPageToken();
            } while (pageToken != null && !pageToken.isBlank());

            // BA-REINDEX-05: Swap alias to new index (atomic)
            try {
                searchIndexAdminPort.swapAlias(alias, newIndexName, existing);
            } catch (Exception ex) {
                throw SearchDomainErrors.reindexAliasSwapFailed(ex.getMessage());
            }

            log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-05", "SWAP_ALIAS",
                "ALIAS_SWAP", "SUCCESS",
                "oldIndex=" + String.join("|", existing) + ",newIndex=" + newIndexName));

            long duration = System.currentTimeMillis() - start;

            // BA-REINDEX-06: Release lock and log completion
            log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-06", "COMPLETE",
                "REINDEX_COMPLETE", "SUCCESS",
                "documentCount=" + indexedCount + ",durationMs=" + duration));

            return new ReindexResult(newIndexName, indexedCount, duration);
        } catch (RuntimeException ex) {
            long duration = System.currentTimeMillis() - start;
            log.info(logLine(USE_CASE_REINDEX, "BA-REINDEX-06", "COMPLETE",
                "REINDEX_COMPLETE", "FAILURE",
                "documentCount=0,durationMs=" + duration));
            throw ex;
        }
    }

    private SearchQuery normalizeSearchQuery(SearchQuery query) {
        List<FacetFilter> normalizedFilters = new ArrayList<>();
        for (FacetFilter filter : query.filters()) {
            if (filter == null) {
                continue;
            }
            String field = normalizeFacetField(filter.field());
            List<String> values = new ArrayList<>();
            for (String value : filter.values()) {
                if (value != null && !value.isBlank()) {
                    values.add(value);
                }
            }
            normalizedFilters.add(new FacetFilter(field, values));
        }

        Language language = query.language() == null ? DEFAULT_LANGUAGE : query.language();
        PriceRange priceRange = query.priceRange();

        return new SearchQuery(
            query.queryText(),
            query.page(),
            query.pageSize(),
            query.sortField(),
            query.sortOrder(),
            normalizedFilters,
            priceRange,
            language
        );
    }

    private AutocompleteQuery normalizeAutocompleteQuery(AutocompleteQuery query) {
        int limit = query.limit();
        if (limit <= 0) {
            limit = DEFAULT_AUTOCOMPLETE_LIMIT;
        }
        if (limit > MAX_AUTOCOMPLETE_LIMIT) {
            limit = MAX_AUTOCOMPLETE_LIMIT;
        }
        return new AutocompleteQuery(query.prefix(), limit, query.language(), query.familyFilter());
    }

    private GetFacetValuesQuery normalizeFacetValuesQuery(GetFacetValuesQuery query) {
        List<String> normalizedFields = new ArrayList<>();
        for (String field : query.fields()) {
            if (field != null && !field.isBlank()) {
                normalizedFields.add(normalizeFacetField(field));
            }
        }
        return new GetFacetValuesQuery(normalizedFields, query.language());
    }

    private void validateFacetFilters(List<FacetFilter> filters) {
        for (FacetFilter filter : filters) {
            String field = normalizeFacetField(filter.field());
            if (!VALID_FACET_FIELDS.contains(field)) {
                throw SearchDomainErrors.invalidFacetField(filter.field());
            }
        }
    }

    private void validateFacetValueFields(List<String> fields) {
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("At least one facet field is required");
        }
        for (String field : fields) {
            String normalized = normalizeFacetField(field);
            if (!VALID_FACET_FIELDS.contains(normalized)) {
                throw SearchDomainErrors.invalidFacetValuesField(field);
            }
        }
    }

    private String normalizeFacetField(String field) {
        if (field == null) {
            return "";
        }
        String normalized = field.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "family" -> "family";
            case "profile_system", "profile-system", "profilesystem" -> "profileSystem";
            case "material", "materials" -> "materials";
            case "color", "colors" -> "colors";
            case "openingtype", "opening_type", "opening-types", "openingtypes" -> "openingTypes";
            default -> normalized;
        };
    }

    private void validateCatalogEvent(CatalogProductEvent event) {
        List<String> missing = new ArrayList<>();

        if (event.productId() == null || event.productId().isBlank()) {
            missing.add("productId");
        }
        if (event.name() == null || event.name().isBlank()) {
            missing.add("name");
        }
        if (event.family() == null || event.family().isBlank()) {
            missing.add("family");
        }
        if (event.description() == null || event.description().isBlank()) {
            missing.add("description");
        }
        if (event.profileSystem() == null || event.profileSystem().isBlank()) {
            missing.add("profileSystem");
        }
        if (event.basePrice() == null) {
            missing.add("basePrice");
        }
        if (event.maxPrice() == null) {
            missing.add("maxPrice");
        }
        if (event.status() == null || event.status() == ProductStatus.UNSPECIFIED) {
            missing.add("status");
        }
        if (event.thumbnailUrl() == null || event.thumbnailUrl().isBlank()) {
            missing.add("thumbnailUrl");
        }

        if (!missing.isEmpty()) {
            throw SearchDomainErrors.invalidIndexEvent("Missing fields: " + missing);
        }
    }

    private ProductSearchDocument toDocument(CatalogProductEvent event) {
        String currency = resolveCurrency(event.basePrice(), event.maxPrice());
        Instant publishedAt = event.publishedAt() != null ? event.publishedAt() : event.updatedAt();
        LocalizedString name = localizedOrNull(event.name());
        LocalizedString description = localizedOrNull(event.description());

        return ProductSearchDocument.builder(event.productId())
            .name(name)
            .description(description)
            .family(event.family())
            .profileSystem(event.profileSystem())
            .openingTypes(event.openingTypes())
            .materials(event.materials())
            .colors(event.colors())
            .minPrice(event.basePrice())
            .maxPrice(event.maxPrice())
            .currency(currency)
            .popularity(event.popularity())
            .status(event.status())
            .publishedAt(publishedAt)
            .thumbnailUrl(event.thumbnailUrl())
            .optionCount(event.optionGroupCount())
            .suggestInputs(buildSuggestInputs(event))
            .build();
    }

    private LocalizedString localizedOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalizedString.of(DEFAULT_LANGUAGE, value.trim());
    }

    private String resolveCurrency(Money basePrice, Money maxPrice) {
        if (basePrice != null) {
            return basePrice.getCurrency().name();
        }
        if (maxPrice != null) {
            return maxPrice.getCurrency().name();
        }
        return Currency.RUB.name();
    }

    private List<String> buildSuggestInputs(CatalogProductEvent event) {
        List<String> inputs = new ArrayList<>();
        if (event.name() != null && !event.name().isBlank()) {
            inputs.add(event.name());
        }
        return inputs;
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value;
    }

    private String logLine(String useCase, String block, String state,
                           String eventType, String decision, String keyValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("[SVC=").append(SERVICE).append("]")
            .append("[UC=").append(useCase).append("]")
            .append("[BLOCK=").append(block).append("]")
            .append("[STATE=").append(state).append("]")
            .append(" eventType=").append(eventType)
            .append(" decision=").append(decision);
        if (keyValues != null && !keyValues.isBlank()) {
            builder.append(" keyValues=").append(keyValues);
        }
        return builder.toString();
    }

    private String nextIndexName(List<String> existingIndices, String versionPrefix) {
        int maxVersion = 0;
        for (String index : existingIndices) {
            if (index == null || !index.startsWith(versionPrefix)) {
                continue;
            }
            String suffix = index.substring(versionPrefix.length());
            try {
                int parsed = Integer.parseInt(suffix);
                if (parsed > maxVersion) {
                    maxVersion = parsed;
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return versionPrefix + (maxVersion + 1);
    }
}
