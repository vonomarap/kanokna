package com.kanokna.cart.application.service.dto;

import java.util.List;

/**
 * Result of checkout validation.
 */
public record CheckoutValidationResult(
    boolean valid,
    int validItemCount,
    int invalidItemCount,
    List<String> invalidItemIds,
    boolean requiresPriceAcknowledgment,
    double priceChangePercent
) {
    public static CheckoutValidationResult valid(int itemCount) {
        return new CheckoutValidationResult(true, itemCount, 0, List.of(), false, 0.0);
    }

    public static CheckoutValidationResult invalidItems(int validCount, int invalidCount, List<String> invalidIds) {
        return new CheckoutValidationResult(false, validCount, invalidCount, invalidIds, false, 0.0);
    }

    public static CheckoutValidationResult requiresAcknowledgment(int itemCount, double changePercent) {
        return new CheckoutValidationResult(false, itemCount, 0, List.of(), true, changePercent);
    }
}
