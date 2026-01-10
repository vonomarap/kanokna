package com.kanokna.cart.application.dto;

import com.kanokna.shared.money.Money;

/**
 * Result of refreshing cart prices.
 */
public record RefreshPricesResult(
    CartDto cart,
    int itemsUpdated,
    boolean totalChanged,
    Money previousTotal,
    double priceChangePercent
) {
}
