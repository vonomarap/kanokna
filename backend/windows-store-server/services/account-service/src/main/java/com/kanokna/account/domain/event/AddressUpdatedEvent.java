package com.kanokna.account.domain.event;

import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AddressUpdatedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String userId,
    String addressId,
    List<String> changedFields
) implements DomainEvent {
    public static AddressUpdatedEvent create(String userId, String addressId, long version, List<String> changedFields) {
        return new AddressUpdatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            userId,
            version,
            userId,
            addressId,
            changedFields
        );
    }

    @Override
    public String aggregateType() {
        return "UserProfile";
    }
}