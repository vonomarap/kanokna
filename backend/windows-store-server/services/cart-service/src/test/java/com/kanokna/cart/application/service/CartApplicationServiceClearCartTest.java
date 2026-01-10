package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.ClearCartCommand;
import com.kanokna.cart.domain.event.CartClearedEvent;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceClearCartTest {
    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-001: clear cart removes all items")
    void clearCartRemovesAllItems() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-60",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-60", "Window", "WINDOW", 1, CartServiceTestFixture.money("100.00"), "hash-60", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-61", "Door", "DOOR", 1, CartServiceTestFixture.money("100.00"), "hash-61", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.clearCart(new ClearCartCommand("cust-60", null));

        assertTrue(result.items().isEmpty());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-002: clear cart removes promo")
    void clearCartRemovesPromo() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-61",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-62", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-62", Instant.now().plusSeconds(3600))
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo("SAVE10", CartServiceTestFixture.money("100.00"));
        cart.applyPromoCode(promo, context.totalsCalculator, null);
        context.cartRepository.save(cart);

        CartDto result = context.service.clearCart(new ClearCartCommand("cust-61", null));

        assertNull(result.appliedPromoCode());
        assertEquals(CartServiceTestFixture.money("0.00"), result.discount());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-003: clear cart resets totals")
    void clearCartResetsTotals() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-62",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-63", "Window", "WINDOW", 1, CartServiceTestFixture.money("10000.00"), "hash-63", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.clearCart(new ClearCartCommand("cust-62", null));

        assertEquals(CartServiceTestFixture.money("0.00"), result.subtotal());
        assertEquals(CartServiceTestFixture.money("0.00"), result.total());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-004: clear empty cart is idempotent")
    void clearEmptyCartIsIdempotent() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = Cart.createForCustomer("cust-63", Currency.RUB);
        context.cartRepository.save(cart);

        CartDto result = context.service.clearCart(new ClearCartCommand("cust-63", null));

        assertTrue(result.items().isEmpty());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-005: CartClearedEvent published with item count")
    void clearedEventPublishedWithItemCount() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-64",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-64", "Window", "WINDOW", 1, CartServiceTestFixture.money("100.00"), "hash-64", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-65", "Door", "DOOR", 1, CartServiceTestFixture.money("100.00"), "hash-65", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-66", "Accessory", "ACCESSORY", 1, CartServiceTestFixture.money("100.00"), "hash-66", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);

        context.service.clearCart(new ClearCartCommand("cust-64", null));

        CartClearedEvent event = context.eventPublisher.events().stream()
            .filter(record -> record.event() instanceof CartClearedEvent)
            .map(record -> (CartClearedEvent) record.event())
            .findFirst()
            .orElseThrow();
        assertEquals(3, event.itemsRemoved());
    }
}
