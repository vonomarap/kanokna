package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;

import java.math.BigDecimal;
import java.math.MathContext;
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
    Money subtotal,
    Money newTotal,
    double discountPercent
) implements DomainEvent {
    public static PromoCodeAppliedEvent create(Cart cart, AppliedPromoCode promo) {
        Money subtotal = cart.totals().subtotal();
        double discountPercent = subtotal.isZero()
            ? 0.0
            : promo.discountAmount().getAmount()
                .divide(subtotal.getAmount(), MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        return new PromoCodeAppliedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            promo.code(),
            promo.discountAmount(),
            subtotal,
            cart.totals().total(),
            discountPercent
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
