package com.kanokna.search.application.dto;

/**
 * Result of a reindex operation.
 */
public record ReindexResult(String newIndexName, long documentCount, long durationMs) {
}
