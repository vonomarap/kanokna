package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartItemAddedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String itemId,
    String productTemplateId,
    String productName,
    int quantity,
    Money unitPrice,
    Money lineTotal,
    String configurationHash
) implements DomainEvent {
    public static CartItemAddedEvent create(Cart cart, CartItem item) {
        return new CartItemAddedEvent(
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
            item.unitPrice(),
            item.lineTotal(),
            item.configurationHash()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
