package com.kanokna.cart.application.dto;

/**
 * Command for refreshing all prices in a cart.
 */
public record RefreshPricesCommand(
    String customerId,
    String sessionId
) {
}
