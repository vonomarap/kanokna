package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ConfigurationDeletedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String configurationId
) implements DomainEvent {
    public static ConfigurationDeletedEvent create(String userId, String configurationId, long version) {
        return new ConfigurationDeletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            configurationId,
            version,
            userId,
            configurationId
        );
    }

    @Override
    public String aggregateType() {
        return "SavedConfiguration";
    }
}