package com.kanokna.catalog.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event: Published when a new catalog version is released.
 * Indicates that catalog content has changed and downstream services should refresh.
 */
public record CatalogVersionPublishedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID catalogVersionId,
    int versionNumber,
    int templateCount,
    String publishedBy
) {

    public CatalogVersionPublishedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
        Objects.requireNonNull(catalogVersionId, "catalogVersionId cannot be null");
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("versionNumber must be positive");
        }
        if (templateCount < 0) {
            throw new IllegalArgumentException("templateCount cannot be negative");
        }
    }

    public static CatalogVersionPublishedEvent create(
        UUID catalogVersionId,
        int versionNumber,
        int templateCount,
        String publishedBy
    ) {
        return new CatalogVersionPublishedEvent(
            UUID.randomUUID(),
            Instant.now(),
            catalogVersionId,
            versionNumber,
            templateCount,
            publishedBy
        );
    }
}
