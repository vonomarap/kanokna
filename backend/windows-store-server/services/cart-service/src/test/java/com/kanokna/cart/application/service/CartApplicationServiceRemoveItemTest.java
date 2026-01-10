package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.RemoveItemCommand;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceRemoveItemTest {
    @Test
    @DisplayName("TC-FUNC-CART-REMOVE-001: remove item recalculates totals")
    void removeItemRecalculatesTotals() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-40",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-40", "Window", "WINDOW", 1, CartServiceTestFixture.money("100.00"), "hash-40", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-41", "Door", "DOOR", 1, CartServiceTestFixture.money("100.00"), "hash-41", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-42", "Accessory", "ACCESSORY", 1, CartServiceTestFixture.money("100.00"), "hash-42", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        String itemId = cart.items().get(1).itemId().toString();

        CartDto result = context.service.removeItem(new RemoveItemCommand("cust-40", null, itemId));

        assertEquals(2, result.items().size());
        assertEquals(CartServiceTestFixture.money("200.00"), result.subtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REMOVE-002: remove last item results in empty cart")
    void removeLastItemResultsInEmptyCart() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-41",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-43", "Window", "WINDOW", 1, CartServiceTestFixture.money("250.00"), "hash-43", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        String itemId = cart.items().get(0).itemId().toString();

        CartDto result = context.service.removeItem(new RemoveItemCommand("cust-41", null, itemId));

        assertTrue(result.items().isEmpty());
        assertEquals(CartServiceTestFixture.money("0.00"), result.subtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REMOVE-003: non-existent item returns error")
    void removeNonExistentItemReturnsError() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-42",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-44", "Window", "WINDOW", 1, CartServiceTestFixture.money("100.00"), "hash-44", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.removeItem(new RemoveItemCommand("cust-42", null, "missing"))
        );

        assertEquals("ERR-CART-ITEM-NOT-FOUND", ex.getCode());
    }
}
