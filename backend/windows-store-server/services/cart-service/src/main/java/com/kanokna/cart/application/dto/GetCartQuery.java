package com.kanokna.cart.application.dto;

/**
 * Query for retrieving an existing cart or returning an empty cart.
 */
public record GetCartQuery(
    String customerId,
    String sessionId
) {
}
