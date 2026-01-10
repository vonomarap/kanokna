package com.kanokna.cart.application.dto;

import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.List;

/**
 * DTO representing an immutable cart snapshot.
 */
public record CartSnapshotDto(
    String snapshotId,
    String cartId,
    String customerId,
    List<CartItemDto> items,
    Money subtotal,
    Money discount,
    Money tax,
    Money total,
    AppliedPromoCodeDto appliedPromoCode,
    int itemCount,
    Instant createdAt,
    Instant validUntil
) {
}
