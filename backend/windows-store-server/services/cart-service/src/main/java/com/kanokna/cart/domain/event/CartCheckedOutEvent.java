package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartSnapshot;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartCheckedOutEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String snapshotId,
    String customerId,
    int itemCount,
    Money total
) implements DomainEvent {
    public static CartCheckedOutEvent create(Cart cart, CartSnapshot snapshot) {
        return new CartCheckedOutEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            snapshot.snapshotId().toString(),
            cart.customerId(),
            cart.totals().itemCount(),
            cart.totals().total()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
