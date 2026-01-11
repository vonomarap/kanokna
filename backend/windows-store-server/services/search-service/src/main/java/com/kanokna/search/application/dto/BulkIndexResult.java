package com.kanokna.search.application.dto;

/**
 * Result of a bulk index operation.
 */
public record BulkIndexResult(long indexedCount, long failedCount) {
}
