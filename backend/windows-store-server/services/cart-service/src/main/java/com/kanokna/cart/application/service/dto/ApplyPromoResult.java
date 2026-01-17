package com.kanokna.cart.application.service.dto;

import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.shared.money.Money;

/**
 * Result of promo code application.
 */
public record ApplyPromoResult(
    boolean success,
    AppliedPromoCode appliedPromoCode,
    Money discount,
    String errorCode,
    String errorMessage
) {
    public static ApplyPromoResult success(AppliedPromoCode promo) {
        return new ApplyPromoResult(true, promo, promo.discountAmount(), null, null);
    }

    public static ApplyPromoResult failure(String errorCode, String errorMessage) {
        return new ApplyPromoResult(false, null, null, errorCode, errorMessage);
    }

    public static ApplyPromoResult unavailable() {
        return new ApplyPromoResult(false, null, null, "ERR-CART-PRICING-UNAVAILABLE", "Pricing service unavailable");
    }
}
