package com.kanokna.catalog_configuration_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Objects;

public final class ProductTemplatePublishedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id templateId;
    private final Id tenantId;
    private final long version;

    private ProductTemplatePublishedEvent(String eventId, Instant occurredAt, Id templateId, Id tenantId, long version) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.version = version;
    }

    public static ProductTemplatePublishedEvent of(Id templateId, Id tenantId, long version) {
        return new ProductTemplatePublishedEvent(Id.random().value(), Instant.now(), templateId, tenantId, version);
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Id templateId() {
        return templateId;
    }

    public Id tenantId() {
        return tenantId;
    }

    public long version() {
        return version;
    }
}
