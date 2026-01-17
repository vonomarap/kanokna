package com.kanokna.cart.application.service.dto;

import com.kanokna.shared.money.Money;

/**
 * Result of price refresh operation for cart items.
 */
public record PriceRefreshResult(
    Money previousTotal,
    Money newTotal,
    double changePercent,
    int itemsUpdated,
    int successCount,
    int failCount,
    boolean totalChanged
) {
    public boolean hasFailures() {
        return failCount > 0;
    }

    public boolean isComplete() {
        return failCount == 0;
    }

    public boolean isPartial() {
        return failCount > 0 && successCount > 0;
    }

    public boolean isTotalFailure() {
        return failCount > 0 && successCount == 0;
    }
}
