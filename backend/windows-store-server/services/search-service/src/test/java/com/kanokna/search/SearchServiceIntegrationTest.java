package com.kanokna.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.google.protobuf.Timestamp;
import com.kanokna.catalog.v1.ProductTemplatePublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUnpublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUpdatedEvent;
import com.kanokna.common.v1.EventMetadata;
import com.kanokna.search.adapters.config.SearchProperties;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.dto.CatalogProductPage;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.GetProductByIdQuery;
import com.kanokna.search.application.dto.ReindexCommand;
import com.kanokna.search.application.dto.ReindexResult;
import com.kanokna.search.application.port.out.CatalogConfigurationPort;
import com.kanokna.search.application.port.out.DistributedLockPort;
import com.kanokna.search.application.port.out.SearchIndexAdminPort;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.application.service.SearchApplicationService;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.FacetFilter;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.domain.model.SortField;
import com.kanokna.search.domain.model.SortOrder;
import com.kanokna.search.support.SearchTestFixture;
import com.kanokna.search.support.TestContainersConfig;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@SpringBootTest
class SearchServiceIntegrationTest extends TestContainersConfig {
    @Autowired
    private SearchApplicationService searchApplicationService;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private SearchIndexAdminPort searchIndexAdminPort;

    @Autowired
    private DistributedLockPort distributedLockPort;

    @Autowired
    private SearchProperties searchProperties;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private CatalogConfigurationPort catalogConfigurationPort;

    @Value("${kafka.topics.product-published}")
    private String productPublishedTopic;

    @Value("${kafka.topics.product-updated}")
    private String productUpdatedTopic;

    @Value("${kafka.topics.product-unpublished}")
    private String productUnpublishedTopic;

    @BeforeEach
    void setUp() throws IOException {
        resetIndices();
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-001: indexProductFromPublishedEvent_thenSearchReturnsProduct")
    void indexProductFromPublishedEvent_thenSearchReturnsProduct() {
        publishEvent(productPublishedTopic, publishedEvent("p1", "Window Alpha"));

        SearchResult result = awaitSearch("", 1);

        assertTrue(result.products().stream().anyMatch(doc -> doc.getId().equals("p1")));
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-002: indexProductFromUpdatedEvent_thenSearchReturnsUpdatedProduct")
    void indexProductFromUpdatedEvent_thenSearchReturnsUpdatedProduct() {
        publishEvent(productPublishedTopic, publishedEvent("p2", "Window Beta"));
        awaitProductById("p2");

        publishEvent(productUpdatedTopic, updatedEvent("p2", "Window Gamma"));

        ProductSearchDocument document = awaitProductById("p2");
        assertEquals("Window Gamma", document.getName().resolve(Language.RU));
    }

    @Test
    @DisplayName("TC-FUNC-DELETE-001: deleteProductFromUnpublishedEvent_thenSearchDoesNotReturnProduct")
    void deleteProductFromUnpublishedEvent_thenSearchDoesNotReturnProduct() {
        publishEvent(productPublishedTopic, publishedEvent("p3", "Window Delete"));
        awaitProductById("p3");

        publishEvent(productUnpublishedTopic, unpublishedEvent("p3"));

        awaitProductMissing("p3");
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-001: reindex_createsNewIndexAndSwapsAlias")
    void reindex_createsNewIndexAndSwapsAlias() {
        CatalogProductEvent event = SearchTestFixture.catalogProductEvent("p10", ProductStatus.ACTIVE);
        CatalogProductPage page = SearchTestFixture.catalogProductPage(List.of(event), null);
        when(catalogConfigurationPort.listProductTemplates(anyInt(), nullable(String.class)))
            .thenReturn(page);

        ReindexResult result = searchApplicationService.reindexCatalog(new ReindexCommand(null));

        assertEquals(searchProperties.index().versionPrefix() + "2", result.newIndexName());
        assertTrue(searchIndexAdminPort.resolveAlias(searchProperties.index().alias())
            .contains(result.newIndexName()));
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-003: reindex_concurrentRequests_secondIsRejected")
    void reindex_concurrentRequests_secondIsRejected() {
        DistributedLockPort.LockHandle handle = distributedLockPort.tryAcquire(
            searchProperties.reindex().lockName());
        assertNotNull(handle);
        try {
            DomainException ex = assertThrows(
                DomainException.class,
                () -> searchApplicationService.reindexCatalog(new ReindexCommand(null))
            );
            assertEquals("ERR-REINDEX-IN-PROGRESS", ex.getCode());
        } finally {
            handle.release();
        }
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-005: search_withFacetFilters_returnsFilteredResults")
    void search_withFacetFilters_returnsFilteredResults() throws IOException {
        searchRepository.index(buildDocument("p20", "Window Prime", "WINDOW", "PVC"));
        searchRepository.index(buildDocument("p21", "Door Prime", "DOOR", "WOOD"));
        refreshAlias();

        SearchQuery query = new SearchQuery(
            "",
            0,
            20,
            SortField.RELEVANCE,
            SortOrder.DESC,
            List.of(new FacetFilter("family", List.of("WINDOW"))),
            null,
            Language.RU
        );

        SearchResult result = searchApplicationService.searchProducts(query);

        assertEquals(1, result.products().size());
        assertEquals("WINDOW", result.products().get(0).getFamily());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-001: autocomplete_returnsMatchingSuggestions")
    void autocomplete_returnsMatchingSuggestions() throws IOException {
        searchRepository.index(buildDocument("p30", "Window Spark", "WINDOW", "PVC"));
        refreshAlias();

        var result = searchApplicationService.autocomplete(
            new AutocompleteQuery("Win", 10, Language.RU, null)
        );

        assertFalse(result.suggestions().isEmpty());
        assertEquals("Window Spark", result.suggestions().get(0).text());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-002: getFacetValues_returnsAllFacetBuckets")
    void getFacetValues_returnsAllFacetBuckets() throws IOException {
        searchRepository.index(buildDocument("p40", "Window Core", "WINDOW", "PVC"));
        searchRepository.index(buildDocument("p41", "Door Core", "DOOR", "WOOD"));
        refreshAlias();

        FacetValuesResult result = searchApplicationService.getFacetValues(
            new GetFacetValuesQuery(List.of("family", "materials"), Language.RU)
        );

        assertEquals(2, result.facets().size());
        assertTrue(result.facets().stream().anyMatch(facet -> "family".equals(facet.field())));
        assertTrue(result.facets().stream().anyMatch(facet -> "materials".equals(facet.field())));
    }

    @Test
    @DisplayName("TC-FUNC-GET-001: getProductById_returnsDocument")
    void getProductById_returnsDocument() throws IOException {
        searchRepository.index(buildDocument("p50", "Window Get", "WINDOW", "PVC"));
        refreshAlias();

        ProductSearchDocument document = searchApplicationService.getProductById(
            new GetProductByIdQuery("p50", Language.RU)
        );

        assertEquals("p50", document.getId());
    }

    private void resetIndices() throws IOException {
        String alias = searchProperties.index().alias();
        String versionPrefix = searchProperties.index().versionPrefix();
        deleteIndex(alias);
        deleteIndex(versionPrefix + "*");

        String baseIndex = versionPrefix + "1";
        searchIndexAdminPort.createIndex(baseIndex);
        searchIndexAdminPort.swapAlias(alias, baseIndex, List.of());
        refreshAlias();
    }

    private void deleteIndex(String name) {
        try {
            elasticsearchClient.indices().delete(d -> d.index(name));
        } catch (ElasticsearchException ex) {
            if (ex.status() != 404) {
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reset indices", ex);
        }
    }

    private void refreshAlias() throws IOException {
        elasticsearchClient.indices().refresh(r -> r.index(searchProperties.index().alias()));
    }

    private void publishEvent(String topic, Object payload) {
        kafkaTemplate.send(topic, payload);
        kafkaTemplate.flush();
    }

    private SearchResult awaitSearch(String queryText, int expectedMin) {
        long deadline = System.currentTimeMillis() + 10_000L;
        RuntimeException lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                refreshAlias();
                SearchResult result = searchApplicationService.searchProducts(
                    new SearchQuery(
                        queryText,
                        0,
                        20,
                        SortField.RELEVANCE,
                        SortOrder.DESC,
                        List.of(),
                        null,
                        Language.RU
                    )
                );
                if (result.products().size() >= expectedMin) {
                    return result;
                }
            } catch (RuntimeException ex) {
                lastError = ex;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          sleep(200);
        }
        if (lastError != null) {
            throw lastError;
        }
        fail("Timed out waiting for search results");
        return null;
    }

    private ProductSearchDocument awaitProductById(String productId) {
        long deadline = System.currentTimeMillis() + 10_000L;
        RuntimeException lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                refreshAlias();
                return searchApplicationService.getProductById(
                    new GetProductByIdQuery(productId, Language.RU)
                );
            } catch (RuntimeException ex) {
                lastError = ex;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          sleep(200);
        }
        if (lastError != null) {
            throw lastError;
        }
        fail("Timed out waiting for product " + productId);
        return null;
    }

    private void awaitProductMissing(String productId) {
        long deadline = System.currentTimeMillis() + 10_000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                refreshAlias();
                searchApplicationService.getProductById(new GetProductByIdQuery(productId, Language.RU));
            } catch (DomainException ex) {
                if ("ERR-GET-PRODUCT-NOT-FOUND".equals(ex.getCode())) {
                    return;
                }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          sleep(200);
        }
        fail("Timed out waiting for product deletion " + productId);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for search", ex);
        }
    }

    private ProductSearchDocument buildDocument(
        String id,
        String name,
        String family,
        String material
    ) {
        return ProductSearchDocument.builder(id)
            .name(LocalizedString.of(Language.RU, name))
            .description(LocalizedString.of(Language.RU, name + " desc"))
            .family(family)
            .profileSystem("REHAU")
            .openingTypes(List.of("TILT"))
            .materials(List.of(material))
            .colors(List.of("WHITE"))
            .minPrice(Money.ofMinor(100_00, Currency.RUB))
            .maxPrice(Money.ofMinor(500_00, Currency.RUB))
            .currency("RUB")
            .popularity(10)
            .status(ProductStatus.ACTIVE)
            .publishedAt(Instant.now())
            .thumbnailUrl("http://example.com/" + id + ".png")
            .optionCount(2)
            .suggestInputs(List.of(name))
            .build();
    }

    private ProductTemplatePublishedEvent publishedEvent(String productId, String name) {
        return ProductTemplatePublishedEvent.newBuilder()
            .setMetadata(eventMetadata("evt-" + productId))
            .setProductTemplateId(productId)
            .setName(name)
            .setProductFamily("WINDOW")
            .setDescription(name + " description")
            .setProfileSystem("REHAU")
            .addAllOpeningTypes(List.of("TILT"))
            .addAllMaterials(List.of("PVC"))
            .addAllColors(List.of("WHITE"))
            .setBasePrice(protoMoney(100_00))
            .setMaxPrice(protoMoney(500_00))
            .setStatus(com.kanokna.catalog.v1.ProductStatus.PRODUCT_STATUS_ACTIVE)
            .setThumbnailUrl("http://example.com/" + productId + ".png")
            .setPopularity(5)
            .setOptionGroupCount(2)
            .setPublishedAt(timestampNow())
            .build();
    }

    private ProductTemplateUpdatedEvent updatedEvent(String productId, String name) {
        return ProductTemplateUpdatedEvent.newBuilder()
            .setMetadata(eventMetadata("evt-" + productId + "-upd"))
            .setProductTemplateId(productId)
            .addAllUpdatedFields(List.of("name", "description"))
            .setPriceChanged(false)
            .setOptionsChanged(false)
            .setName(name)
            .setProductFamily("WINDOW")
            .setDescription(name + " description")
            .setProfileSystem("REHAU")
            .addAllOpeningTypes(List.of("TILT"))
            .addAllMaterials(List.of("PVC"))
            .addAllColors(List.of("WHITE"))
            .setBasePrice(protoMoney(110_00))
            .setMaxPrice(protoMoney(510_00))
            .setStatus(com.kanokna.catalog.v1.ProductStatus.PRODUCT_STATUS_ACTIVE)
            .setThumbnailUrl("http://example.com/" + productId + ".png")
            .setPopularity(6)
            .setOptionGroupCount(3)
            .setUpdatedAt(timestampNow())
            .build();
    }

    private ProductTemplateUnpublishedEvent unpublishedEvent(String productId) {
        return ProductTemplateUnpublishedEvent.newBuilder()
            .setMetadata(eventMetadata("evt-" + productId + "-del"))
            .setProductTemplateId(productId)
            .setReason("unpublished")
            .build();
    }

    private EventMetadata eventMetadata(String eventId) {
        return EventMetadata.newBuilder()
            .setEventId(eventId)
            .setOccurredAt(timestampNow())
            .setAggregateId(UUID.randomUUID().toString())
            .setAggregateType("ProductTemplate")
            .setCorrelationId(UUID.randomUUID().toString())
            .build();
    }

    private com.kanokna.common.v1.Money protoMoney(long minor) {
        return com.kanokna.common.v1.Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(com.kanokna.common.v1.Currency.CURRENCY_RUB)
            .build();
    }

    private Timestamp timestampNow() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
            .setSeconds(now.getEpochSecond())
            .setNanos(now.getNano())
            .build();
    }
}
