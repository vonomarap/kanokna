package com.kanokna.cart.domain.model;

import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartTest {
    @Test
    @DisplayName("TC-FUNC-CART-ADD-005: addItem recalculates totals")
    void addItemRecalculatesTotals() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        Cart cart = Cart.createForCustomer("cust-1", Currency.RUB);
        Money unitPrice = CartServiceTestFixture.money("1000.00");
        CartItem item = CartServiceTestFixture.item(
            "T-1",
            "Window",
            "WINDOW",
            2,
            unitPrice,
            "hash-1",
            Instant.now().plusSeconds(3600)
        );

        cart.addItem(item, calculator, null);

        assertEquals(1, cart.items().size());
        assertEquals(CartServiceTestFixture.money("2000.00"), cart.totals().subtotal());
        assertEquals(CartServiceTestFixture.money("2000.00"), cart.totals().total());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-003: clear resets totals and promo")
    void clearResetsTotalsAndPromo() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-2",
            calculator,
            CartServiceTestFixture.item(
                "T-2",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("5000.00"),
                "hash-2",
                Instant.now().plusSeconds(3600)
            )
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo(
            "SAVE10",
            CartServiceTestFixture.money("500.00")
        );
        cart.applyPromoCode(promo, calculator, null);

        cart.clear(calculator);

        assertTrue(cart.items().isEmpty());
        assertNull(cart.appliedPromoCode());
        assertTrue(cart.totals().subtotal().isZero());
        assertTrue(cart.totals().total().isZero());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-002: markCheckedOut sets status")
    void markCheckedOutSetsStatus() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-3",
            calculator,
            CartServiceTestFixture.item(
                "T-3",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1200.00"),
                "hash-3",
                Instant.now().plusSeconds(3600)
            )
        );

        cart.markCheckedOut();

        assertEquals(CartStatus.CHECKED_OUT, cart.status());
    }
}
