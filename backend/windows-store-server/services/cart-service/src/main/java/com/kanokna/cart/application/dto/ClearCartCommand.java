package com.kanokna.cart.application.dto;

/**
 * Command for clearing all items from a cart.
 */
public record ClearCartCommand(
    String customerId,
    String sessionId
) {
}
