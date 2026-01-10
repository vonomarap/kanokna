package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartItemUpdatedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String itemId,
    int oldQuantity,
    int newQuantity,
    Money oldLineTotal,
    Money newLineTotal,
    Money cartSubtotal
) implements DomainEvent {
    public static CartItemUpdatedEvent create(Cart cart, CartItem item, int oldQuantity, Money oldLineTotal) {
        return new CartItemUpdatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            item.itemId().toString(),
            oldQuantity,
            item.quantity(),
            oldLineTotal,
            item.lineTotal(),
            cart.totals().subtotal()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
