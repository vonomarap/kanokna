package com.kanokna.search.application.dto;

/**
 * Result of a bulk index operation.
 */
public class BulkIndexResult {
    private final long indexedCount;
    private final long failedCount;

    public BulkIndexResult(long indexedCount, long failedCount) {
        this.indexedCount = indexedCount;
        this.failedCount = failedCount;
    }

    public long getIndexedCount() {
        return indexedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }
}
