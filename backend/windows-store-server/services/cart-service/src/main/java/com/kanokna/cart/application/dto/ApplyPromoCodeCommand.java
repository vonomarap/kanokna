package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for applying a promo code to the cart.
 */
public record ApplyPromoCodeCommand(
    String customerId,
    String sessionId,
    @NotBlank String promoCode
) {
}
