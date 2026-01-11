package com.kanokna.search.application.dto;

/**
 * Result of an index operation.
 */
public record IndexResult(boolean success, String documentId, long tookMs) {
}
