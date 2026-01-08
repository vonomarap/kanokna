package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AddressAddedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String addressId,
    boolean isDefault
) implements DomainEvent {
    public static AddressAddedEvent create(String userId, String addressId, boolean isDefault, long version) {
        return new AddressAddedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            userId,
            version,
            userId,
            addressId,
            isDefault
        );
    }

    @Override
    public String aggregateType() {
        return "UserProfile";
    }
}