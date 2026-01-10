package com.kanokna.cart.domain.event;

import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.UUID;

public record CartMergedEvent(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    long version,
    String sourceCartId,
    String targetCartId,
    String customerId,
    int itemsMergedCount,
    int itemsQuantitySummed,
    int itemsAddedNew,
    String promoCodeSource,
    String promoCode,
    Money finalTotal
) implements DomainEvent {
    public static CartMergedEvent create(
        Cart source,
        Cart target,
        CartMergeService.MergeResult mergeResult
    ) {
        return new CartMergedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            target.cartId().toString(),
            target.version(),
            source.cartId().toString(),
            target.cartId().toString(),
            target.customerId(),
            mergeResult.itemsFromAnonymous(),
            mergeResult.itemsMerged(),
            mergeResult.itemsAdded(),
            mergeResult.promoCodeSource(),
            target.appliedPromoCode() == null ? null : target.appliedPromoCode().code(),
            target.totals().total()
        );
    }

    @Override
    public String aggregateType() {
        return "Cart";
    }
}
