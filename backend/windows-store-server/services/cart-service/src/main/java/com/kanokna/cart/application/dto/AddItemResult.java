package com.kanokna.cart.application.dto;

/**
 * Result of adding an item to a cart.
 */
public record AddItemResult(
    CartDto cart,
    String addedItemId
) {
}
