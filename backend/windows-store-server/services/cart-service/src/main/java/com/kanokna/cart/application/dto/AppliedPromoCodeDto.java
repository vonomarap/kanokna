package com.kanokna.cart.application.dto;

import com.kanokna.shared.money.Money;
import java.time.Instant;

/**
 * Applied promo code details for API responses.
 */
public record AppliedPromoCodeDto(
    String code,
    Money discountAmount,
    String description,
    Instant appliedAt
) {
}
