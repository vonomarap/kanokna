package com.kanokna.cart.application.service.dto;

/**
 * Result of cart merge operation.
 */
public record MergeResult(
    int itemsFromSource,
    int itemsMerged,
    int itemsAdded,
    boolean promoCodePreserved,
    String promoCodeSource
) {
    public int totalItemsTransferred() {
        return itemsMerged + itemsAdded;
    }
}
