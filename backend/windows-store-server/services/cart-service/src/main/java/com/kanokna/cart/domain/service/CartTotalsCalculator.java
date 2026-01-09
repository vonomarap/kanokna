package com.kanokna.cart.domain.service;

import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartTotals;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.util.List;

/**
 * Calculates cart totals from items and promo codes.
 */
public class CartTotalsCalculator {
    public CartTotals calculateTotals(List<CartItem> items, AppliedPromoCode promo, Money taxOverride) {
        List<CartItem> safeItems = items == null ? List.of() : items;
        Currency currency = resolveCurrency(safeItems, promo, taxOverride);
        Money subtotal = Money.zero(currency);
        int itemCount = 0;
        for (CartItem item : safeItems) {
            subtotal = subtotal.add(item.lineTotal());
            itemCount += item.quantity();
        }

        Money discount = promo == null ? Money.zero(currency) : promo.discountAmount();
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        Money tax = taxOverride != null ? taxOverride : Money.zero(currency);
        Money total = subtotal.subtract(discount).add(tax);

        return new CartTotals(subtotal, discount, tax, total, itemCount);
    }

    private Currency resolveCurrency(List<CartItem> items, AppliedPromoCode promo, Money taxOverride) {
        if (!items.isEmpty()) {
            return items.getFirst().unitPrice().getCurrency();
        }
        if (promo != null) {
            return promo.discountAmount().getCurrency();
        }
        if (taxOverride != null) {
            return taxOverride.getCurrency();
        }
        return Currency.RUB;
    }
}
