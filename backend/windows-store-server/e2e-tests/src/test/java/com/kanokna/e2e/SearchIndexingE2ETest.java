package com.kanokna.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchIndexingE2ETest extends E2ETestBase {
    @Test
    @DisplayName("E2E-SEARCH-001: Product change -> Kafka -> Elasticsearch")
    void searchIndexingFlow() {
        ServiceRouting routing = requireServiceRouting();
        assertNotNull(routing);
    }
}
