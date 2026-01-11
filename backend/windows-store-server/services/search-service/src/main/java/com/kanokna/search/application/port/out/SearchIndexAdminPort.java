package com.kanokna.search.application.port.out;

import java.util.List;

/**
 * Outbound port for index management operations.
 */
public interface SearchIndexAdminPort {
    boolean indexExists(String indexName);

    void createIndex(String indexName);

    List<String> resolveAlias(String alias);

    void swapAlias(String alias, String newIndex, List<String> removeIndices);
}
