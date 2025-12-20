package com.kanokna.order_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;

import java.time.Instant;
import java.util.Objects;

public final class PaymentAppliedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id orderId;
    private final Id paymentId;
    private final String status;
    private final Money amount;

    private PaymentAppliedEvent(String eventId, Instant occurredAt, Id orderId, Id paymentId, String status, Money amount) {
        this.eventId = Objects.requireNonNull(eventId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.orderId = Objects.requireNonNull(orderId);
        this.paymentId = Objects.requireNonNull(paymentId);
        this.status = status;
        this.amount = Objects.requireNonNull(amount);
    }

    public static PaymentAppliedEvent of(Id orderId, Id paymentId, String status, Money amount) {
        return new PaymentAppliedEvent(Id.random().value(), Instant.now(), orderId, paymentId, status, amount);
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

    public Id paymentId() {
        return paymentId;
    }

    public String status() {
        return status;
    }

    public Money amount() {
        return amount;
    }
}
