package com.kanokna.pricing.domain.model;

/**
 * Type of discount applied to a quote.
 * Per DEC-PRICING-DISCOUNT-PRECEDENCE.
 */
public enum DiscountType {
    /** Campaign discount (applied first) */
    CAMPAIGN,

    /** Promo code discount (applied second) */
    PROMO_CODE
}
