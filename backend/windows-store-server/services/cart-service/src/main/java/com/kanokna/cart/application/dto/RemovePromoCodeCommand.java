package com.kanokna.cart.application.dto;

/**
 * Command for removing a promo code from the cart.
 */
public record RemovePromoCodeCommand(
    String customerId,
    String sessionId
) {
}
