package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserProfileCreatedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String email
) implements DomainEvent {
    public static UserProfileCreatedEvent create(String userId, String email, long version) {
        return new UserProfileCreatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            userId,
            version,
            userId,
            email
        );
    }

    @Override
    public String aggregateType() {
        return "UserProfile";
    }
}