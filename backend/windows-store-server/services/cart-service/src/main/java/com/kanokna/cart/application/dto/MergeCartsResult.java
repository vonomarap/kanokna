package com.kanokna.cart.application.dto;

/**
 * Result of merging an anonymous cart into an authenticated cart.
 */
public record MergeCartsResult(
    CartDto mergedCart,
    int itemsFromAnonymous,
    int itemsMerged,
    int itemsAdded,
    boolean promoCodePreserved,
    String promoCodeSource
) {
}
