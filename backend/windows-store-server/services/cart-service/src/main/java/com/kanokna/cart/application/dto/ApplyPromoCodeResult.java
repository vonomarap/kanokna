package com.kanokna.cart.application.dto;

/**
 * Result of applying a promo code.
 */
public record ApplyPromoCodeResult(
    CartDto cart,
    boolean applied,
    String errorMessage,
    String errorCode
) {
}
