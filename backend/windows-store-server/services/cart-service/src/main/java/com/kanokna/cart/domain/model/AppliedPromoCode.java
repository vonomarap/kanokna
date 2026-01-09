package com.kanokna.cart.domain.model;

import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.Objects;

/**
 * Applied promotional code details.
 */
public record AppliedPromoCode(
    String code,
    Money discountAmount,
    String description,
    Instant appliedAt
) {
    public AppliedPromoCode {
        Objects.requireNonNull(code, "code cannot be null");
        Objects.requireNonNull(discountAmount, "discountAmount cannot be null");
        Objects.requireNonNull(appliedAt, "appliedAt cannot be null");
    }
}
