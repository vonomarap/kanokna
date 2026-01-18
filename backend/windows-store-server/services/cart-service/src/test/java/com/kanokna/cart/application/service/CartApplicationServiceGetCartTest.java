package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.GetCartQuery;
import com.kanokna.cart.domain.exception.CartDomainException;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartItemId;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.support.CartServiceTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceGetCartTest {
    @Test
    @DisplayName("TC-FUNC-CART-GET-001: existing cart returns items and totals")
    void getExistingCartReturnsItems() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-10",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-10",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-10",
                Instant.now().plusSeconds(3600)
            ),
            CartServiceTestFixture.item(
                "T-11",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("2000.00"),
                "hash-11",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.getCart(new GetCartQuery("cust-10", null));

        assertEquals(2, result.items().size());
        assertEquals(CartServiceTestFixture.money("3000.00"), result.subtotal());
        assertEquals(2, result.itemCount());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-002: missing cart returns empty cart")
    void missingCartReturnsEmpty() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();

        CartDto result = context.service.getCart(new GetCartQuery("cust-11", null));

        assertNotNull(result.cartId());
        assertTrue(result.items().isEmpty());
        assertEquals(0, result.itemCount());
        assertEquals(CartStatus.ACTIVE, result.status());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-003: session cart resolves for anonymous")
    void sessionCartResolves() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithSession(
            "sess-1",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-12",
                "Accessory",
                "ACCESSORY",
                1,
                CartServiceTestFixture.money("100.00"),
                "hash-12",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.getCart(new GetCartQuery(null, "sess-1"));

        assertEquals("sess-1", result.sessionId());
        assertEquals(1, result.items().size());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-004: invalid configuration flagged")
    void invalidConfigurationFlagged() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.catalogPort.setValid(false);
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-12",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-13",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("900.00"),
                "hash-13",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.getCart(new GetCartQuery("cust-12", null));

        assertEquals(ValidationStatus.INVALID, result.items().get(0).validationStatus());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-005: catalog unavailable keeps last status")
    void catalogUnavailableUsesLastStatus() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.catalogPort.setAvailable(false);
        CartItem item = CartItem.rehydrate(
            CartItemId.generate(),
            "T-14",
            "Window",
            "WINDOW",
            new ConfigurationSnapshot("T-14", 100, 120, List.of(), List.of()),
            "hash-14",
            1,
            CartServiceTestFixture.money("1000.00"),
            new PriceQuoteReference("QUOTE-14", Instant.now().plusSeconds(3600)),
            ValidationStatus.UNKNOWN,
            null,
            null,
            false,
            Instant.now(),
            Instant.now()
        );
        Cart cart = CartServiceTestFixture.cartWithItems("cust-13", context.totalsCalculator, item);
        context.cartRepository.save(cart);

        CartDto result = context.service.getCart(new GetCartQuery("cust-13", null));

        assertEquals(ValidationStatus.UNKNOWN, result.items().get(0).validationStatus());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-006: missing identifiers returns error code")
    void getCartMissingIdentifiersReturnsErrorCode() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();

        CartDomainException ex = assertThrows(CartDomainException.class,
            () -> context.service.getCart(new GetCartQuery(null, null)));

        assertEquals("ERR-CART-MISSING-PARAM", ex.getErrorCode());
    }
}
