package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Command for updating a cart item quantity.
 */
public record UpdateItemCommand(
    String customerId,
    String sessionId,
    @NotBlank String itemId,
    @Min(1) int quantity
) {
}
