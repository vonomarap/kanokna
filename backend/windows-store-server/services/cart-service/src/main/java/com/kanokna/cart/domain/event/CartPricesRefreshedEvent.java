package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartPricesRefreshedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String cartId,
    String customerId,
    int itemsWithChanges,
    Money previousTotal,
    Money newTotal,
    Money totalChange,
    double changePercent,
    String refreshTrigger
) implements DomainEvent {
    public static CartPricesRefreshedEvent create(
        Cart cart,
        int itemsWithChanges,
        Money previousTotal,
        Money newTotal,
        Money totalChange,
        double changePercent,
        String refreshTrigger
    ) {
        return new CartPricesRefreshedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            cart.cartId().toString(),
            cart.version(),
            cart.cartId().toString(),
            cart.customerId(),
            itemsWithChanges,
            previousTotal,
            newTotal,
            totalChange,
            changePercent,
            refreshTrigger
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
