package com.kanokna.cart.application.dto;

import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.List;

/**
 * DTO representing a shopping cart.
 */
public record CartDto(
    String cartId,
    String customerId,
    String sessionId,
    CartStatus status,
    List<CartItemDto> items,
    Money subtotal,
    Money discount,
    Money tax,
    Money total,
    AppliedPromoCodeDto appliedPromoCode,
    int itemCount,
    Instant createdAt,
    Instant updatedAt
) {
}
