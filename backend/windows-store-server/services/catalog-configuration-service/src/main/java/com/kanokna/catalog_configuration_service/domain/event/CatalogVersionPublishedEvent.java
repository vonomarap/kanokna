package com.kanokna.catalog_configuration_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Objects;

public final class CatalogVersionPublishedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id catalogVersionId;
    private final Id tenantId;
    private final int versionNumber;

    private CatalogVersionPublishedEvent(
        String eventId,
        Instant occurredAt,
        Id catalogVersionId,
        Id tenantId,
        int versionNumber
    ) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.catalogVersionId = Objects.requireNonNull(catalogVersionId, "catalogVersionId");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.versionNumber = versionNumber;
    }

    public static CatalogVersionPublishedEvent of(Id catalogVersionId, Id tenantId, int versionNumber) {
        return new CatalogVersionPublishedEvent(Id.random().value(), Instant.now(), catalogVersionId, tenantId, versionNumber);
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Id catalogVersionId() {
        return catalogVersionId;
    }

    public Id tenantId() {
        return tenantId;
    }

    public int versionNumber() {
        return versionNumber;
    }
}
