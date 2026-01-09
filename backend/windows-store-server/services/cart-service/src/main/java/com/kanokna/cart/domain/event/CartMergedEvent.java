package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CartMergedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String sourceCartId,
    String targetCartId,
    String customerId,
    int itemsMergedCount
) implements DomainEvent {
    public static CartMergedEvent create(Cart source, Cart target, int itemsMergedCount) {
        return new CartMergedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            target.cartId().toString(),
            target.version(),
            source.cartId().toString(),
            target.cartId().toString(),
            target.customerId(),
            itemsMergedCount
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
