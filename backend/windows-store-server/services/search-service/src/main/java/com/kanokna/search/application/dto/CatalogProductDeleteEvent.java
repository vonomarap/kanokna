package com.kanokna.search.application.dto;

/**
 * Catalog product delete event payload.
 */
public record CatalogProductDeleteEvent(String eventId, String productId) {
}
