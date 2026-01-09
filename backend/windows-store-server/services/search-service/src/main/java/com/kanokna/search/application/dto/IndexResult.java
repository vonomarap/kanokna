package com.kanokna.search.application.dto;

/**
 * Result of an index operation.
 */
public class IndexResult {
    private final boolean success;
    private final String documentId;
    private final long tookMs;

    public IndexResult(boolean success, String documentId, long tookMs) {
        this.success = success;
        this.documentId = documentId;
        this.tookMs = tookMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDocumentId() {
        return documentId;
    }

    public long getTookMs() {
        return tookMs;
    }
}
