package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CartCreatedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String sessionId,
    boolean anonymous
) implements DomainEvent {
    public static CartCreatedEvent create(Cart cart) {
        return new CartCreatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            cart.sessionId(),
            cart.customerId() == null || cart.customerId().isBlank()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
