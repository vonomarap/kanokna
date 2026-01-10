package com.kanokna.cart.domain.service;

import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartTotals;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartTotalsCalculatorTest {
    @Test
    @DisplayName("TC-FUNC-CART-PROMO-001: discount applied to totals")
    void discountAppliedToTotals() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        CartItem item = CartServiceTestFixture.item(
            "T-7",
            "Window",
            "WINDOW",
            1,
            CartServiceTestFixture.money("10000.00"),
            "hash-7",
            Instant.now().plusSeconds(3600)
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo(
            "SAVE10",
            CartServiceTestFixture.money("1000.00")
        );

        CartTotals totals = calculator.calculateTotals(List.of(item), promo, Money.zero(Currency.RUB));

        assertEquals(CartServiceTestFixture.money("10000.00"), totals.subtotal());
        assertEquals(CartServiceTestFixture.money("1000.00"), totals.discount());
        assertEquals(CartServiceTestFixture.money("9000.00"), totals.total());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-005: discount capped at subtotal")
    void discountCappedAtSubtotal() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        CartItem item = CartServiceTestFixture.item(
            "T-8",
            "Door",
            "DOOR",
            1,
            CartServiceTestFixture.money("5000.00"),
            "hash-8",
            Instant.now().plusSeconds(3600)
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo(
            "HUGE",
            CartServiceTestFixture.money("9000.00")
        );

        CartTotals totals = calculator.calculateTotals(List.of(item), promo, Money.zero(Currency.RUB));

        assertEquals(CartServiceTestFixture.money("5000.00"), totals.discount());
        assertEquals(CartServiceTestFixture.money("0.00"), totals.total());
    }
}
