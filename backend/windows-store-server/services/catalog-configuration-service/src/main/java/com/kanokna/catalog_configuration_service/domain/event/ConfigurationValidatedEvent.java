package com.kanokna.catalog_configuration_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Objects;

public final class ConfigurationValidatedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id templateId;
    private final Id tenantId;
    private final long catalogVersion;
    private final String configurationSignature;

    private ConfigurationValidatedEvent(
        String eventId,
        Instant occurredAt,
        Id templateId,
        Id tenantId,
        long catalogVersion,
        String configurationSignature
    ) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.catalogVersion = catalogVersion;
        this.configurationSignature = Objects.requireNonNull(configurationSignature, "configurationSignature");
    }

    public static ConfigurationValidatedEvent of(Id templateId, Id tenantId, long catalogVersion, String configurationSignature) {
        return new ConfigurationValidatedEvent(
            Id.random().value(),
            Instant.now(),
            templateId,
            tenantId,
            catalogVersion,
            configurationSignature
        );
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

    public long catalogVersion() {
        return catalogVersion;
    }

    public String configurationSignature() {
        return configurationSignature;
    }
}
