package com.kanokna.catalog_configuration_service.domain.event;

import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class BomResolvedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id templateId;
    private final Id tenantId;
    private final long catalogVersion;
    private final String bomTemplateCode;
    private final List<BomTemplate.BomItem> items;

    private BomResolvedEvent(
        String eventId,
        Instant occurredAt,
        Id templateId,
        Id tenantId,
        long catalogVersion,
        String bomTemplateCode,
        List<BomTemplate.BomItem> items
    ) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.catalogVersion = catalogVersion;
        this.bomTemplateCode = Objects.requireNonNull(bomTemplateCode, "bomTemplateCode");
        this.items = List.copyOf(items);
    }

    public static BomResolvedEvent of(Id templateId, Id tenantId, long catalogVersion, String bomTemplateCode, List<BomTemplate.BomItem> items) {
        return new BomResolvedEvent(
            Id.random().value(),
            Instant.now(),
            templateId,
            tenantId,
            catalogVersion,
            bomTemplateCode,
            items
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

    public String bomTemplateCode() {
        return bomTemplateCode;
    }

    public List<BomTemplate.BomItem> items() {
        return items;
    }
}
