package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartAbandonedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    Instant lastActivity,
    int itemCount,
    Money subtotal
) implements DomainEvent {
    public static CartAbandonedEvent create(Cart cart, Instant lastActivity) {
        return new CartAbandonedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            lastActivity,
            cart.totals().itemCount(),
            cart.totals().subtotal()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
