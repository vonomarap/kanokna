package com.kanokna.order_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Objects;

public final class OrderCreatedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id orderId;
    private final Id customerId;
    private final String status;

    private OrderCreatedEvent(String eventId, Instant occurredAt, Id orderId, Id customerId, String status) {
        this.eventId = Objects.requireNonNull(eventId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.orderId = Objects.requireNonNull(orderId);
        this.customerId = customerId;
        this.status = status;
    }

    public static OrderCreatedEvent of(Id orderId, Id customerId, String status) {
        return new OrderCreatedEvent(Id.random().value(), Instant.now(), orderId, customerId, status);
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Id orderId() {
        return orderId;
    }

    public Id customerId() {
        return customerId;
    }

    public String status() {
        return status;
    }
}
