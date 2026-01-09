package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CartItemRemovedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String itemId,
    String productTemplateId
) implements DomainEvent {
    public static CartItemRemovedEvent create(Cart cart, CartItem item) {
        return new CartItemRemovedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            item.itemId().toString(),
            item.productTemplateId()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
