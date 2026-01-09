package com.kanokna.search.application.dto;

/**
 * Catalog product delete event payload.
 */
public class CatalogProductDeleteEvent {
    private final String eventId;
    private final String productId;

    public CatalogProductDeleteEvent(String eventId, String productId) {
        this.eventId = eventId;
        this.productId = productId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getProductId() {
        return productId;
    }
}
