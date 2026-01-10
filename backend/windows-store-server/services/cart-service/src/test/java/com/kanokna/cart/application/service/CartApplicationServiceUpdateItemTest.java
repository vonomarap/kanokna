package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.CartItemDto;
import com.kanokna.cart.application.dto.UpdateItemCommand;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartApplicationServiceUpdateItemTest {
    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-001: update quantity recalculates line total")
    void updateQuantityRecalculatesLineTotal() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-30",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-30",
                "Window",
                "WINDOW",
                2,
                CartServiceTestFixture.money("1000.00"),
                "hash-30",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);
        String itemId = cart.items().get(0).itemId().toString();

        CartDto result = context.service.updateItem(new UpdateItemCommand("cust-30", null, itemId, 5));

        CartItemDto updated = result.items().get(0);
        assertEquals(5, updated.quantity());
        assertEquals(CartServiceTestFixture.money("5000.00"), updated.lineTotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-002: totals recalculated after update")
    void totalsRecalculatedAfterUpdate() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-31",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-31",
                "Door",
                "DOOR",
                5,
                CartServiceTestFixture.money("200.00"),
                "hash-31",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);
        String itemId = cart.items().get(0).itemId().toString();

        CartDto result = context.service.updateItem(new UpdateItemCommand("cust-31", null, itemId, 2));

        assertEquals(CartServiceTestFixture.money("400.00"), result.subtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-003: non-existent item returns error")
    void nonExistentItemReturnsError() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-32",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-32",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("500.00"),
                "hash-32",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.updateItem(new UpdateItemCommand("cust-32", null, "missing", 2))
        );

        assertEquals("ERR-CART-ITEM-NOT-FOUND", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-004: quantity 0 rejected")
    void quantityZeroRejected() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-33",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-33",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("500.00"),
                "hash-33",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);
        String itemId = cart.items().get(0).itemId().toString();

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.updateItem(new UpdateItemCommand("cust-33", null, itemId, 0))
        );

        assertEquals("ERR-CART-INVALID-QUANTITY", ex.getCode());
    }
}
