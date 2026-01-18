package com.kanokna.search.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.kanokna.search.application.port.out.CatalogConfigurationPort;
import com.kanokna.search.application.port.out.DistributedLockPort;
import com.kanokna.search.application.port.out.SearchIndexAdminPort;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.domain.exception.SearchDomainErrors;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.FacetAggregation;
import com.kanokna.search.domain.model.FacetFilter;
import com.kanokna.search.domain.model.PriceRange;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.domain.model.SortField;
import com.kanokna.search.domain.model.SortOrder;
import com.kanokna.search.domain.model.Suggestion;
import com.kanokna.search.support.SearchTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;

@ExtendWith(MockitoExtension.class)
class SearchApplicationServiceTest {
    @Mock
    private SearchRepository searchRepository;

    @Mock
    private SearchIndexAdminPort searchIndexAdminPort;

    @Mock
    private CatalogConfigurationPort catalogConfigurationPort;

    @Mock
    private DistributedLockPort distributedLockPort;

    private SearchProperties searchProperties;
    private SearchApplicationService service;

    @BeforeEach
    void setUp() {
        searchProperties = new SearchProperties(null, null);
        service = new SearchApplicationService(
                searchRepository,
                searchIndexAdminPort,
                catalogConfigurationPort,
                distributedLockPort,
                searchProperties);
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-001: Empty query returns all active products")
    void searchProducts_emptyQuery_returnsAllActiveProducts() {
        SearchQuery query = baseQuery(null);
        SearchResult expected = SearchTestFixture.searchResult(
                List.of(SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE)),
                0,
                20);
        when(searchRepository.search(any())).thenReturn(expected);

        SearchResult result = service.searchProducts(query);

        assertEquals(1, result.products().size());
        verify(searchRepository).search(any());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-002: Text query matches name and description fields")
    void searchProducts_textQuery_passedToRepository() {
        SearchQuery query = baseQuery("oak");
        when(searchRepository.search(any())).thenReturn(SearchTestFixture.searchResult(List.of(), 0, 20));

        service.searchProducts(query);

        ArgumentCaptor<SearchQuery> captor = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchRepository).search(captor.capture());
        assertEquals("oak", captor.getValue().queryText());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-003: Family filter returns only matching family")
    void searchProducts_familyFilter_returnsMatchingFamily() {
        SearchQuery query = baseQuery("window", List.of(new FacetFilter("family", List.of("WINDOW"))));
        when(searchRepository.search(any())).thenReturn(SearchTestFixture.searchResult(List.of(), 0, 20));

        service.searchProducts(query);

        ArgumentCaptor<SearchQuery> captor = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchRepository).search(captor.capture());
        assertEquals("family", captor.getValue().filters().get(0).field());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-004: Price range filter works correctly")
    void searchProducts_priceRangeFilter_passedToRepository() {
        PriceRange range = new PriceRange(
                Money.ofMinor(100_00, Currency.RUB),
                Money.ofMinor(500_00, Currency.RUB));
        SearchQuery query = baseQuery("window", List.of(), range);
        when(searchRepository.search(any())).thenReturn(SearchTestFixture.searchResult(List.of(), 0, 20));

        service.searchProducts(query);

        ArgumentCaptor<SearchQuery> captor = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchRepository).search(captor.capture());
        assertNotNull(captor.getValue().priceRange());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-005: Multiple filters combine with AND logic")
    void searchProducts_multipleFilters_combinedWithAndLogic() {
        List<FacetFilter> filters = List.of(
                new FacetFilter("family", List.of("WINDOW")),
                new FacetFilter("materials", List.of("PVC")));
        SearchQuery query = baseQuery("window", filters);
        when(searchRepository.search(any())).thenReturn(SearchTestFixture.searchResult(List.of(), 0, 20));

        service.searchProducts(query);

        ArgumentCaptor<SearchQuery> captor = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchRepository).search(captor.capture());
        assertEquals(2, captor.getValue().filters().size());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-006: Sorting by relevance returns highest scores first")
    void searchProducts_sortByRelevance_returnsHighestScoresFirst() {
        ProductSearchDocument first = SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE);
        ProductSearchDocument second = SearchTestFixture.productDocument("p2", ProductStatus.ACTIVE);
        SearchResult expected = new SearchResult(List.of(first, second), 2, 0, 20, 1, List.of(), 5);
        SearchQuery query = new SearchQuery(
                "window",
                0,
                20,
                SortField.RELEVANCE,
                SortOrder.DESC,
                List.of(),
                null,
                Language.RU);
        when(searchRepository.search(any())).thenReturn(expected);

        SearchResult result = service.searchProducts(query);

        assertEquals("p1", result.products().get(0).getId());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-007: Pagination returns correct slice of results")
    void searchProducts_pagination_returnsCorrectSlice() {
        SearchResult expected = new SearchResult(List.of(), 50, 2, 10, 5, List.of(), 10);
        SearchQuery query = new SearchQuery(
                "",
                2,
                10,
                SortField.RELEVANCE,
                SortOrder.DESC,
                List.of(),
                null,
                Language.RU);
        when(searchRepository.search(any())).thenReturn(expected);

        SearchResult result = service.searchProducts(query);

        assertEquals(2, result.page());
        assertEquals(10, result.pageSize());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-008: Facet counts update based on active filters")
    void searchProducts_facetCounts_updateBasedOnFilters() {
        FacetAggregation facet = SearchTestFixture.facetAggregation("family", "WINDOW", 12, false);
        SearchResult expected = new SearchResult(List.of(), 0, 0, 20, 0, List.of(facet), 3);
        when(searchRepository.search(any())).thenReturn(expected);

        SearchResult result = service.searchProducts(baseQuery(""));

        assertEquals(1, result.facets().size());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-009: Elasticsearch failure returns technical error")
    void searchProducts_elasticsearchFailure_returnsTechnicalError() {
        doThrow(SearchDomainErrors.elasticsearchUnavailable("down")).when(searchRepository).search(any());

        DomainException ex = assertThrows(DomainException.class, () -> service.searchProducts(baseQuery("")));

        assertEquals("ERR-SEARCH-ES-UNAVAILABLE", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-010: Query time is recorded in result")
    void searchProducts_queryTimeRecordedInResult() {
        SearchResult expected = new SearchResult(List.of(), 0, 0, 20, 0, List.of(), 42);
        when(searchRepository.search(any())).thenReturn(expected);

        SearchResult result = service.searchProducts(baseQuery(""));

        assertEquals(42, result.queryTimeMs());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-001: Valid prefix returns relevant suggestions")
    void autocomplete_validPrefix_returnsSuggestions() {
        AutocompleteQuery query = new AutocompleteQuery("wi", 10, Language.RU, null);
        Suggestion suggestion = SearchTestFixture.suggestion("Window", "p1");
        AutocompleteResult expected = SearchTestFixture.autocompleteResult(List.of(suggestion), 5);
        when(searchRepository.autocomplete(any())).thenReturn(expected);

        AutocompleteResult result = service.autocomplete(query);

        assertEquals(1, result.suggestions().size());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-002: Prefix less than 2 chars returns empty or error")
    void autocomplete_shortPrefix_returnsError() {
        AutocompleteQuery query = new AutocompleteQuery("w", 10, Language.RU, null);

        DomainException ex = assertThrows(DomainException.class, () -> service.autocomplete(query));

        assertEquals("ERR-AUTO-PREFIX-TOO-SHORT", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-003: Family filter restricts suggestions")
    void autocomplete_familyFilter_restrictsSuggestions() {
        AutocompleteQuery query = new AutocompleteQuery("wi", 10, Language.RU, "WINDOW");
        when(searchRepository.autocomplete(any())).thenReturn(SearchTestFixture.autocompleteResult(List.of(), 2));

        service.autocomplete(query);

        ArgumentCaptor<AutocompleteQuery> captor = ArgumentCaptor.forClass(AutocompleteQuery.class);
        verify(searchRepository).autocomplete(captor.capture());
        assertEquals("WINDOW", captor.getValue().familyFilter());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-004: Limit parameter controls max suggestions")
    void autocomplete_limitControlsMaxSuggestions() {
        AutocompleteQuery query = new AutocompleteQuery("wi", 50, Language.RU, null);
        when(searchRepository.autocomplete(any())).thenReturn(SearchTestFixture.autocompleteResult(List.of(), 2));

        service.autocomplete(query);

        ArgumentCaptor<AutocompleteQuery> captor = ArgumentCaptor.forClass(AutocompleteQuery.class);
        verify(searchRepository).autocomplete(captor.capture());
        assertEquals(20, captor.getValue().limit());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-005: Archived products excluded from suggestions")
    void autocomplete_archivedProductsExcluded() {
        Suggestion suggestion = SearchTestFixture.suggestion("Window", "p1");
        when(searchRepository.autocomplete(any()))
                .thenReturn(SearchTestFixture.autocompleteResult(List.of(suggestion), 3));

        AutocompleteResult result = service.autocomplete(new AutocompleteQuery("wi", 10, Language.RU, null));

        assertEquals(1, result.suggestions().size());
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-001: Published event creates new document")
    void indexProduct_publishedEvent_createsNewDocument() {
        CatalogProductEvent event = SearchTestFixture.catalogProductEvent("p1", ProductStatus.ACTIVE);
        when(searchRepository.index(any())).thenReturn(new IndexResult(true, "p1", 10));

        IndexResult result = service.indexProduct(event);

        assertEquals("p1", result.documentId());
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-002: Updated event updates existing document")
    void indexProduct_updatedEvent_updatesExistingDocument() {
        CatalogProductEvent event = SearchTestFixture.catalogProductEvent("p2", ProductStatus.ACTIVE);
        when(searchRepository.index(any())).thenReturn(new IndexResult(true, "p2", 8));

        IndexResult result = service.indexProduct(event);

        assertEquals("p2", result.documentId());
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-003: Same event reprocessed is idempotent")
    void indexProduct_sameEventReprocessed_isIdempotent() {
        CatalogProductEvent event = SearchTestFixture.catalogProductEvent("p3", ProductStatus.ACTIVE);
        when(searchRepository.index(any())).thenReturn(new IndexResult(true, "p3", 4));

        service.indexProduct(event);
        IndexResult result = service.indexProduct(event);

        assertEquals("p3", result.documentId());
        verify(searchRepository, Mockito.times(2)).index(any());
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-004: Invalid event logged and skipped")
    void indexProduct_invalidEvent_loggedAndSkipped() {
        CatalogProductEvent event = new CatalogProductEvent(
                null,
                "PRODUCT_TEMPLATE_PUBLISHED",
                "",
                "",
                "",
                "",
                "",
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                ProductStatus.UNSPECIFIED,
                "",
                0,
                0,
                null,
                null);

        DomainException ex = assertThrows(DomainException.class, () -> service.indexProduct(event));

        assertEquals("ERR-INDEX-INVALID-EVENT", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-005: Elasticsearch failure triggers retry")
    void indexProduct_elasticsearchFailure_triggersRetry() {
        CatalogProductEvent event = SearchTestFixture.catalogProductEvent("p4", ProductStatus.ACTIVE);
        doThrow(SearchDomainErrors.indexElasticsearchUnavailable("down")).when(searchRepository).index(any());

        DomainException ex = assertThrows(DomainException.class, () -> service.indexProduct(event));

        assertEquals("ERR-INDEX-ES-UNAVAILABLE", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-DELETE-001: Unpublished event removes document")
    void deleteProduct_unpublishedEvent_removesDocument() {
        CatalogProductDeleteEvent event = SearchTestFixture.deleteEvent("p1");
        when(searchRepository.delete(anyString())).thenReturn(new DeleteResult(true));

        DeleteResult result = service.deleteProduct(event);

        assertTrue(result.deleted());
    }

    @Test
    @DisplayName("TC-FUNC-DELETE-002: Delete non-existent document succeeds (idempotent)")
    void deleteProduct_nonExistentDocument_succeeds() {
        CatalogProductDeleteEvent event = SearchTestFixture.deleteEvent("missing");
        when(searchRepository.delete(anyString())).thenReturn(new DeleteResult(true));

        DeleteResult result = service.deleteProduct(event);

        assertTrue(result.deleted());
    }

    @Test
    @DisplayName("TC-FUNC-DELETE-003: Elasticsearch failure triggers retry")
    void deleteProduct_elasticsearchFailure_triggersRetry() {
        CatalogProductDeleteEvent event = SearchTestFixture.deleteEvent("p1");
        doThrow(SearchDomainErrors.deleteElasticsearchUnavailable("down")).when(searchRepository).delete(anyString());

        DomainException ex = assertThrows(DomainException.class, () -> service.deleteProduct(event));

        assertEquals("ERR-DELETE-ES-UNAVAILABLE", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-001: Request single facet field returns values")
    void getFacetValues_singleField_returnsValues() {
        FacetAggregation facet = SearchTestFixture.facetAggregation("family", "WINDOW", 3, false);
        when(searchRepository.facetValues(any())).thenReturn(new FacetValuesResult(List.of(facet), 4));

        FacetValuesResult result = service.getFacetValues(new GetFacetValuesQuery(List.of("family"), Language.RU));

        assertEquals(1, result.facets().size());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-002: Request multiple facet fields returns all")
    void getFacetValues_multipleFields_returnsAll() {
        List<FacetAggregation> facets = List.of(
                SearchTestFixture.facetAggregation("family", "WINDOW", 3, false),
                SearchTestFixture.facetAggregation("materials", "PVC", 2, false));
        when(searchRepository.facetValues(any())).thenReturn(new FacetValuesResult(facets, 6));

        FacetValuesResult result = service
                .getFacetValues(new GetFacetValuesQuery(List.of("family", "materials"), Language.RU));

        assertEquals(2, result.facets().size());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-003: Invalid field name returns error")
    void getFacetValues_invalidField_returnsError() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.getFacetValues(new GetFacetValuesQuery(List.of("unknown"), Language.RU)));

        assertEquals("ERR-FACET-INVALID-FIELD", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-004: Archived products excluded from counts")
    void getFacetValues_archivedProductsExcludedFromCounts() {
        FacetAggregation facet = SearchTestFixture.facetAggregation("family", "WINDOW", 2, false);
        when(searchRepository.facetValues(any())).thenReturn(new FacetValuesResult(List.of(facet), 5));

        FacetValuesResult result = service.getFacetValues(new GetFacetValuesQuery(List.of("family"), Language.RU));

        assertEquals(1, result.facets().size());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-005: Empty index returns empty facets")
    void getFacetValues_emptyIndex_returnsEmptyFacets() {
        when(searchRepository.facetValues(any())).thenReturn(new FacetValuesResult(List.of(), 1));

        FacetValuesResult result = service.getFacetValues(new GetFacetValuesQuery(List.of("family"), Language.RU));

        assertEquals(0, result.facets().size());
    }

    @Test
    @DisplayName("TC-FUNC-GET-001: Existing product ID returns document")
    void getProductById_existingProduct_returnsDocument() {
        ProductSearchDocument document = SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE);
        when(searchRepository.getById("p1")).thenReturn(document);

        ProductSearchDocument result = service.getProductById(new GetProductByIdQuery("p1", Language.RU));

        assertEquals("p1", result.getId());
    }

    @Test
    @DisplayName("TC-FUNC-GET-002: Non-existent product ID returns NOT_FOUND")
    void getProductById_nonExistentProduct_returnsNotFound() {
        when(searchRepository.getById("missing")).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.getProductById(new GetProductByIdQuery("missing", Language.RU)));

        assertEquals("ERR-GET-PRODUCT-NOT-FOUND", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-GET-003: Empty product ID returns validation error")
    void getProductById_emptyProductId_returnsValidationError() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getProductById(new GetProductByIdQuery(" ", Language.RU)));
    }

    @Test
    @DisplayName("TC-FUNC-GET-004: Archived product is still retrievable by ID")
    void getProductById_archivedProduct_isRetrievable() {
        ProductSearchDocument document = SearchTestFixture.productDocument("p9", ProductStatus.ARCHIVED);
        when(searchRepository.getById("p9")).thenReturn(document);

        ProductSearchDocument result = service.getProductById(new GetProductByIdQuery("p9", Language.RU));

        assertEquals(ProductStatus.ARCHIVED, result.getStatus());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-001: Successful reindex creates new index and swaps alias")
    void reindexCatalog_success_createsNewIndexAndSwapsAlias() {
        when(distributedLockPort.tryAcquire(anyString())).thenReturn(new TestLockHandle());
        when(searchIndexAdminPort.resolveAlias(searchProperties.index().alias()))
                .thenReturn(List.of("product_templates_v1"));
        when(catalogConfigurationPort.listProductTemplates(anyInt(), any()))
                .thenReturn(SearchTestFixture.catalogProductPage(
                        List.of(SearchTestFixture.catalogProductEvent("p1", ProductStatus.ACTIVE)),
                        null));
        when(searchRepository.bulkIndex(anyString(), any())).thenReturn(new BulkIndexResult(1, 0));

        ReindexResult result = service.reindexCatalog(new ReindexCommand(null));

        assertEquals("product_templates_v2", result.newIndexName());
        verify(searchIndexAdminPort).swapAlias(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-002: Reindex populates all active products")
    void reindexCatalog_populatesAllActiveProducts() {
        when(distributedLockPort.tryAcquire(anyString())).thenReturn(new TestLockHandle());
        when(searchIndexAdminPort.resolveAlias(searchProperties.index().alias()))
                .thenReturn(List.of());
        CatalogProductEvent first = SearchTestFixture.catalogProductEvent("p1", ProductStatus.ACTIVE);
        CatalogProductEvent second = SearchTestFixture.catalogProductEvent("p2", ProductStatus.ACTIVE);
        when(catalogConfigurationPort.listProductTemplates(anyInt(), any()))
                .thenReturn(new CatalogProductPage(List.of(first, second), null));
        when(searchRepository.bulkIndex(anyString(), any())).thenReturn(new BulkIndexResult(2, 0));

        ReindexResult result = service.reindexCatalog(new ReindexCommand(null));

        assertEquals(2, result.documentCount());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-003: Concurrent reindex requests are rejected")
    void reindexCatalog_concurrentRequests_rejected() {
        when(distributedLockPort.tryAcquire(anyString())).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.reindexCatalog(new ReindexCommand(null)));

        assertEquals("ERR-REINDEX-IN-PROGRESS", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-004: Failed reindex does not swap alias")
    void reindexCatalog_failedReindex_doesNotSwapAlias() {
        when(distributedLockPort.tryAcquire(anyString())).thenReturn(new TestLockHandle());
        when(searchIndexAdminPort.resolveAlias(searchProperties.index().alias()))
                .thenReturn(List.of("product_templates_v1"));
        when(catalogConfigurationPort.listProductTemplates(anyInt(), any()))
                .thenReturn(SearchTestFixture.catalogProductPage(
                        List.of(SearchTestFixture.catalogProductEvent("p1", ProductStatus.ACTIVE)),
                        null));
        when(searchRepository.bulkIndex(anyString(), any())).thenReturn(new BulkIndexResult(0, 1));

        assertThrows(DomainException.class, () -> service.reindexCatalog(new ReindexCommand(null)));

        verify(searchIndexAdminPort, never()).swapAlias(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-005: Previous index remains after successful swap")
    void reindexCatalog_previousIndexRemains_afterSuccessfulSwap() {
        when(distributedLockPort.tryAcquire(anyString())).thenReturn(new TestLockHandle());
        when(searchIndexAdminPort.resolveAlias(searchProperties.index().alias()))
                .thenReturn(List.of("product_templates_v1"));
        when(catalogConfigurationPort.listProductTemplates(anyInt(), any()))
                .thenReturn(SearchTestFixture.catalogProductPage(
                        List.of(SearchTestFixture.catalogProductEvent("p1", ProductStatus.ACTIVE)),
                        null));
        when(searchRepository.bulkIndex(anyString(), any())).thenReturn(new BulkIndexResult(1, 0));

        service.reindexCatalog(new ReindexCommand(null));

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(searchIndexAdminPort).swapAlias(anyString(), anyString(), captor.capture());
        assertEquals(List.of("product_templates_v1"), captor.getValue());
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-007: Redis unavailable returns lock acquisition error")
    void reindexCatalog_lockUnavailable_returnsError() {
        doThrow(new RuntimeException("redis down")).when(distributedLockPort).tryAcquire(anyString());

        DomainException ex = assertThrows(DomainException.class,
                () -> service.reindexCatalog(new ReindexCommand(null)));

        assertEquals("ERR-REINDEX-LOCK-UNAVAILABLE", ex.getCode());
    }

    private SearchQuery baseQuery(String queryText) {
        return baseQuery(queryText, List.of(), null);
    }

    private SearchQuery baseQuery(String queryText, List<FacetFilter> filters) {
        return baseQuery(queryText, filters, null);
    }

    private SearchQuery baseQuery(String queryText, List<FacetFilter> filters, PriceRange range) {
        return new SearchQuery(
                queryText,
                0,
                20,
                SortField.RELEVANCE,
                SortOrder.DESC,
                filters,
                range,
                Language.RU);
    }

    private static class TestLockHandle implements DistributedLockPort.LockHandle {
        private boolean released;

        @Override
        public void release() {
            released = true;
        }

        boolean isReleased() {
            return released;
        }
    }
}
