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
    Money total,
    String appliedPromoCode,
    Money discountAmount,
    long cartAgeSeconds
) implements DomainEvent {
    public static CartCheckedOutEvent create(Cart cart, CartSnapshot snapshot) {
        Money discount = cart.appliedPromoCode() == null
            ? Money.zero(cart.totals().subtotal().getCurrency())
            : cart.appliedPromoCode().discountAmount();
        return new CartCheckedOutEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            snapshot.snapshotId().toString(),
            cart.customerId(),
            cart.totals().itemCount(),
            cart.totals().total(),
            cart.appliedPromoCode() == null ? null : cart.appliedPromoCode().code(),
            discount,
            java.time.Duration.between(cart.createdAt(), snapshot.createdAt()).getSeconds()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
