package com.kanokna.order_service.domain.model;

import com.kanokna.shared.money.Money;

import java.util.Objects;

public record Totals(
    Money subtotal,
    Money discounts,
    Money shipping,
    Money installation,
    Money tax,
    Money deposit,
    Money grandTotal
) {
    public Totals {
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(discounts, "discounts");
        Objects.requireNonNull(shipping, "shipping");
        Objects.requireNonNull(installation, "installation");
        Objects.requireNonNull(tax, "tax");
        Objects.requireNonNull(deposit, "deposit");
        Objects.requireNonNull(grandTotal, "grandTotal");
        ensureSameCurrency(subtotal, discounts, shipping, installation, tax, deposit, grandTotal);
    }

    private void ensureSameCurrency(Money... monies) {
        var currency = monies[0].getCurrency();
        for (Money money : monies) {
            if (!currency.equals(money.getCurrency())) {
                throw new IllegalArgumentException("Totals must share same currency");
            }
        }
    }
}
