package com.kanokna.catalog.domain.event;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import com.kanokna.catalog.domain.model.ProductTemplateId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event: Audit trail for configuration validations.
 * Can be used for analytics on validation failures and configuration patterns.
 */
public record ConfigurationValidatedEvent(
    UUID eventId,
    Instant occurredAt,
    ProductTemplateId productTemplateId,
    boolean valid,
    int errorCount
) {

    public ConfigurationValidatedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        if (errorCount < 0) {
            throw CatalogDomainErrors.invalidErrorCount(errorCount);
        }
    }

    public static ConfigurationValidatedEvent create(
        ProductTemplateId productTemplateId,
        boolean valid,
        int errorCount
    ) {
        return new ConfigurationValidatedEvent(
            UUID.randomUUID(),
            Instant.now(),
            productTemplateId,
            valid,
            errorCount
        );
    }
}
