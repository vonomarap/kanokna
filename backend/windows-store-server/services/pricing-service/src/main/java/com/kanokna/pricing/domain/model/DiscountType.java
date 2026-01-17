package com.kanokna.pricing.domain.model;

/**
 * Discount value type for campaigns and promo codes.
 */
public enum DiscountType {
    /** Percentage discount (applied to subtotal) */
    PERCENTAGE,
    /** Fixed amount discount */
    FIXED
}
