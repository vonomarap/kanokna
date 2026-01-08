package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ConfigurationSavedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String configurationId,
    String productTemplateId
) implements DomainEvent {
    public static ConfigurationSavedEvent create(String userId, String configurationId, String productTemplateId, long version) {
        return new ConfigurationSavedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            configurationId,
            version,
            userId,
            configurationId,
            productTemplateId
        );
    }

    @Override
    public String aggregateType() {
        return "SavedConfiguration";
    }
}