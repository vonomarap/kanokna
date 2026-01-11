package com.kanokna.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.search.application.dto.BulkIndexResult;
import com.kanokna.search.application.port.out.SearchIndexAdminPort;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.support.TestContainersConfig;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ElasticsearchIntegrationTest extends TestContainersConfig {
    private static final String INDEX_PREFIX = "search_it_";

    @Autowired
    private SearchIndexAdminPort searchIndexAdminPort;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deleteIndex(INDEX_PREFIX + "*");
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-001: indexMapping_createdCorrectly")
    void indexMapping_createdCorrectly() throws Exception {
        String indexName = INDEX_PREFIX + "mapping_" + UUID.randomUUID();
        searchIndexAdminPort.createIndex(indexName);

        Map<String, Object> payload = fetchMapping(indexName);
        Map<String, Object> properties = extractProperties(payload, indexName);

        assertTrue(properties.containsKey("name"));
        assertTrue(properties.containsKey("suggest"));
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-005: aliasSwap_atomic")
    void aliasSwap_atomic() {
        String alias = INDEX_PREFIX + "alias_" + UUID.randomUUID();
        String index1 = alias + "_v1";
        String index2 = alias + "_v2";
        searchIndexAdminPort.createIndex(index1);
        searchIndexAdminPort.createIndex(index2);

        searchIndexAdminPort.swapAlias(alias, index1, List.of());
        assertTrue(searchIndexAdminPort.resolveAlias(alias).contains(index1));

        searchIndexAdminPort.swapAlias(alias, index2, List.of(index1));
        List<String> resolved = searchIndexAdminPort.resolveAlias(alias);

        assertEquals(1, resolved.size());
        assertEquals(index2, resolved.get(0));
    }

    @Test
    @DisplayName("TC-FUNC-REINDEX-002: bulkIndex_handlesLargeBatches")
    void bulkIndex_handlesLargeBatches() {
        String indexName = INDEX_PREFIX + "bulk_" + UUID.randomUUID();
        searchIndexAdminPort.createIndex(indexName);

        List<ProductSearchDocument> documents = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            documents.add(buildDocument("bulk-" + i, "Window " + i, "WINDOW"));
        }

        BulkIndexResult result = searchRepository.bulkIndex(indexName, documents);

        assertEquals(120, result.indexedCount());
        assertEquals(0, result.failedCount());
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractProperties(Map<String, Object> payload, String indexName) {
        Map<String, Object> indexNode = (Map<String, Object>) payload.get(indexName);
        Map<String, Object> mappings = (Map<String, Object>) indexNode.get("mappings");
        return (Map<String, Object>) mappings.get("properties");
    }

    private Map<String, Object> fetchMapping(String indexName) throws Exception {
        Request request = new Request("GET", "/" + indexName + "/_mapping");
        Response response = restClient.performRequest(request);
        try (InputStream content = response.getEntity().getContent()) {
            return objectMapper.readValue(content, new TypeReference<>() {});
        }
    }

    private ProductSearchDocument buildDocument(String id, String name, String family) {
        return ProductSearchDocument.builder(id)
            .name(LocalizedString.of(Language.RU, name))
            .description(LocalizedString.of(Language.RU, name + " desc"))
            .family(family)
            .profileSystem("REHAU")
            .openingTypes(List.of("TILT"))
            .materials(List.of("PVC"))
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
}
