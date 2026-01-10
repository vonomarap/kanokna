package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartClearedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String sessionId,
    int itemsRemoved,
    Money clearedSubtotal,
    boolean promoCodeRemoved
) implements DomainEvent {
    public static CartClearedEvent create(Cart cart,
                                          int itemsRemoved,
                                          Money clearedSubtotal,
                                          boolean promoCodeRemoved) {
        return new CartClearedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            cart.sessionId(),
            itemsRemoved,
            clearedSubtotal,
            promoCodeRemoved
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
