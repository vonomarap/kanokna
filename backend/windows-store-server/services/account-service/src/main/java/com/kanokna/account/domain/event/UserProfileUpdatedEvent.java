package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileUpdatedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    List<String> changedFields
) implements DomainEvent {
    public static UserProfileUpdatedEvent create(String userId, long version, List<String> changedFields) {
        return new UserProfileUpdatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            userId,
            version,
            userId,
            changedFields
        );
    }

    @Override
    public String aggregateType() {
        return "UserProfile";
    }
}