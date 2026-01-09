package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record PromoCodeAppliedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    String promoCode,
    Money discountAmount,
    Money newTotal
) implements DomainEvent {
    public static PromoCodeAppliedEvent create(Cart cart, AppliedPromoCode promo) {
        return new PromoCodeAppliedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            promo.code(),
            promo.discountAmount(),
            cart.totals().total()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
