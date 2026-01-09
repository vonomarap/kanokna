package com.kanokna.search.application.dto;

/**
 * Result of a reindex operation.
 */
public class ReindexResult {
    private final String newIndexName;
    private final long documentCount;
    private final long durationMs;

    public ReindexResult(String newIndexName, long documentCount, long durationMs) {
        this.newIndexName = newIndexName;
        this.documentCount = documentCount;
        this.durationMs = durationMs;
    }

    public String getNewIndexName() {
        return newIndexName;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
