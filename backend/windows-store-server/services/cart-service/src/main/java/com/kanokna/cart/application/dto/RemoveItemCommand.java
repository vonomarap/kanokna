package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for removing an item from the cart.
 */
public record RemoveItemCommand(
    String customerId,
    String sessionId,
    @NotBlank String itemId
) {
}
