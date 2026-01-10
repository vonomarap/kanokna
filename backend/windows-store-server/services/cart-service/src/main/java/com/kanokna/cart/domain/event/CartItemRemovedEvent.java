package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
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
    String productTemplateId,
    String productName,
    int quantityRemoved,
    Money lineTotalRemoved,
    int cartItemCount,
    Money cartSubtotal
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
            item.productTemplateId(),
            item.productName(),
            item.quantity(),
            item.lineTotal(),
            cart.totals().itemCount(),
            cart.totals().subtotal()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
