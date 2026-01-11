package com.kanokna.search.application.dto;

/**
 * Command for reindexing the catalog.
 */
public record ReindexCommand(String sourceIndex) {
}
