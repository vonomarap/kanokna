package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PromoCodeRemovedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String promoCode
) implements DomainEvent {
    public static PromoCodeRemovedEvent create(Cart cart, String promoCode) {
        return new PromoCodeRemovedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            promoCode
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
