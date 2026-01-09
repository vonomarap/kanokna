package com.kanokna.cart.domain.model;

import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.util.Objects;

/**
 * Calculated cart totals.
 */
public record CartTotals(
    Money subtotal,
    Money discount,
    Money tax,
    Money total,
    int itemCount
) {
    public CartTotals {
        Objects.requireNonNull(subtotal, "subtotal cannot be null");
        Objects.requireNonNull(discount, "discount cannot be null");
        Objects.requireNonNull(tax, "tax cannot be null");
        Objects.requireNonNull(total, "total cannot be null");
    }

    public static CartTotals empty(Currency currency) {
        Objects.requireNonNull(currency, "currency cannot be null");
        Money zero = Money.zero(currency);
        return new CartTotals(zero, zero, zero, zero, 0);
    }
}
