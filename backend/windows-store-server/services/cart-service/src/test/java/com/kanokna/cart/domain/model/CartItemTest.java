package com.kanokna.cart.domain.model;

import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartItemTest {
    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-001: updateQuantity recalculates line total")
    void updateQuantityRecalculatesLineTotal() {
        Money unitPrice = CartServiceTestFixture.money("1500.00");
        CartItem item = CartServiceTestFixture.item(
            "T-4",
            "Window",
            "WINDOW",
            2,
            unitPrice,
            "hash-4",
            Instant.now().plusSeconds(3600)
        );

        item.updateQuantity(5);

        assertEquals(5, item.quantity());
        assertEquals(unitPrice.multiplyBy(BigDecimal.valueOf(5)), item.lineTotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-004: quantity 0 rejected")
    void updateQuantityRejectsZero() {
        CartItem item = CartServiceTestFixture.item(
            "T-5",
            "Door",
            "DOOR",
            1,
            CartServiceTestFixture.money("800.00"),
            "hash-5",
            Instant.now().plusSeconds(3600)
        );

        assertThrows(IllegalArgumentException.class, () -> item.updateQuantity(0));
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-002: clearPriceStale resets flag")
    void clearPriceStaleResetsFlag() {
        CartItem item = CartServiceTestFixture.item(
            "T-6",
            "Accessory",
            "ACCESSORY",
            1,
            CartServiceTestFixture.money("50.00"),
            "hash-6",
            Instant.now().plusSeconds(3600)
        );

        item.markPriceStale(true);
        item.clearPriceStale();

        assertFalse(item.priceStale());
    }
}
