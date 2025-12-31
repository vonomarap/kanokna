package com.kanokna.catalog.domain.event;

import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplateId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event: Published when a product template becomes ACTIVE.
 * Triggers search index update in search-service.
 */
public record ProductTemplatePublishedEvent(
    UUID eventId,
    Instant occurredAt,
    ProductTemplateId productTemplateId,
    String productName,
    ProductFamily productFamily,
    long version
) {

    public ProductTemplatePublishedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        Objects.requireNonNull(productName, "productName cannot be null");
        Objects.requireNonNull(productFamily, "productFamily cannot be null");
    }

    public static ProductTemplatePublishedEvent create(
        ProductTemplateId productTemplateId,
        String productName,
        ProductFamily productFamily,
        long version
    ) {
        return new ProductTemplatePublishedEvent(
            UUID.randomUUID(),
            Instant.now(),
            productTemplateId,
            productName,
            productFamily,
            version
        );
    }
}
