package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AddressDeletedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String addressId
) implements DomainEvent {
    public static AddressDeletedEvent create(String userId, String addressId, long version) {
        return new AddressDeletedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            userId,
            version,
            userId,
            addressId
        );
    }

    @Override
    public String aggregateType() {
        return "UserProfile";
    }
}