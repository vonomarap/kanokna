package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.MergeCartsCommand;
import com.kanokna.cart.application.dto.MergeCartsResult;
import com.kanokna.cart.domain.exception.CartDomainException;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.support.CartServiceTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceMergeTest {
    @Test
    @DisplayName("TC-FUNC-CART-MERGE-001: Merge combines items from both carts")
    void mergeCombinesItemsFromBothCarts() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart authCart = CartServiceTestFixture.cartWithItems(
            "cust-merge-1",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-70",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-70",
                Instant.now().plusSeconds(3600)
            ),
            CartServiceTestFixture.item(
                "T-71",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("2000.00"),
                "hash-71",
                Instant.now().plusSeconds(3600)
            )
        );
        Cart anonCart = CartServiceTestFixture.cartWithSession(
            "sess-merge-1",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-72",
                "Accessory",
                "ACCESSORY",
                1,
                CartServiceTestFixture.money("500.00"),
                "hash-72",
                Instant.now().plusSeconds(3600)
            ),
            CartServiceTestFixture.item(
                "T-73",
                "Window",
                "WINDOW",
                2,
                CartServiceTestFixture.money("700.00"),
                "hash-73",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(authCart);
        context.cartRepository.save(anonCart);

        MergeCartsResult result = context.service.mergeCarts(new MergeCartsCommand(
            "cust-merge-1",
            "sess-merge-1"
        ));

        assertEquals(4, result.mergedCart().items().size());
        assertEquals(2, result.itemsFromAnonymous());
        assertEquals(0, result.itemsMerged());
        assertEquals(2, result.itemsAdded());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-002: Same configuration items have quantities summed")
    void mergeSumsQuantitiesForSameConfiguration() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart authCart = CartServiceTestFixture.cartWithItems(
            "cust-merge-2",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-74",
                "Window",
                "WINDOW",
                2,
                CartServiceTestFixture.money("1000.00"),
                "hash-74",
                Instant.now().plusSeconds(3600)
            )
        );
        Cart anonCart = CartServiceTestFixture.cartWithSession(
            "sess-merge-2",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-74",
                "Window",
                "WINDOW",
                3,
                CartServiceTestFixture.money("1000.00"),
                "hash-74",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(authCart);
        context.cartRepository.save(anonCart);

        MergeCartsResult result = context.service.mergeCarts(new MergeCartsCommand(
            "cust-merge-2",
            "sess-merge-2"
        ));

        assertEquals(1, result.mergedCart().items().size());
        assertEquals(1, result.itemsMerged());
        assertEquals(0, result.itemsAdded());
        assertEquals(5, result.mergedCart().items().get(0).quantity());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-003: Anonymous cart deleted after merge")
    void mergeMarksAnonymousCartMerged() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart authCart = CartServiceTestFixture.cartWithItems(
            "cust-merge-3",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-75",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-75",
                Instant.now().plusSeconds(3600)
            )
        );
        Cart anonCart = CartServiceTestFixture.cartWithSession(
            "sess-merge-3",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-76",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("900.00"),
                "hash-76",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(authCart);
        context.cartRepository.save(anonCart);

        context.service.mergeCarts(new MergeCartsCommand("cust-merge-3", "sess-merge-3"));

        Cart mergedAnon = context.cartRepository.findBySessionId("sess-merge-3").orElse(null);
        assertNotNull(mergedAnon);
        assertEquals(CartStatus.MERGED, mergedAnon.status());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-004: Authenticated promo code preserved")
    void mergePreservesAuthenticatedPromo() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart authCart = CartServiceTestFixture.cartWithItems(
            "cust-merge-4",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-77",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-77",
                Instant.now().plusSeconds(3600)
            )
        );
        authCart.applyPromoCode(
            CartServiceTestFixture.promo("AUTH10", CartServiceTestFixture.money("100.00")),
            context.totalsCalculator,
            null
        );
        Cart anonCart = CartServiceTestFixture.cartWithSession(
            "sess-merge-4",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-78",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("1200.00"),
                "hash-78",
                Instant.now().plusSeconds(3600)
            )
        );
        anonCart.applyPromoCode(
            CartServiceTestFixture.promo("ANON20", CartServiceTestFixture.money("200.00")),
            context.totalsCalculator,
            null
        );
        context.cartRepository.save(authCart);
        context.cartRepository.save(anonCart);

        MergeCartsResult result = context.service.mergeCarts(new MergeCartsCommand(
            "cust-merge-4",
            "sess-merge-4"
        ));

        assertTrue(result.promoCodePreserved());
        assertEquals("AUTHENTICATED", result.promoCodeSource());
        assertEquals("AUTH10", result.mergedCart().appliedPromoCode().code());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-005: Anonymous promo adopted if no auth promo")
    void mergeAdoptsAnonymousPromoWhenNoAuthPromo() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart authCart = CartServiceTestFixture.cartWithItems(
            "cust-merge-5",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-79",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("900.00"),
                "hash-79",
                Instant.now().plusSeconds(3600)
            )
        );
        Cart anonCart = CartServiceTestFixture.cartWithSession(
            "sess-merge-5",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-80",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("1300.00"),
                "hash-80",
                Instant.now().plusSeconds(3600)
            )
        );
        anonCart.applyPromoCode(
            CartServiceTestFixture.promo("ANON20", CartServiceTestFixture.money("200.00")),
            context.totalsCalculator,
            null
        );
        context.cartRepository.save(authCart);
        context.cartRepository.save(anonCart);

        MergeCartsResult result = context.service.mergeCarts(new MergeCartsCommand(
            "cust-merge-5",
            "sess-merge-5"
        ));

        assertTrue(result.promoCodePreserved());
        assertEquals("ANONYMOUS", result.promoCodeSource());
        assertEquals("ANON20", result.mergedCart().appliedPromoCode().code());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-006: missing identifiers returns error code")
    void mergeMissingIdentifiersReturnsErrorCode() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();

        CartDomainException ex = assertThrows(CartDomainException.class,
            () -> context.service.mergeCarts(new MergeCartsCommand(null, null)));

        assertEquals("ERR-CART-MISSING-PARAM", ex.getErrorCode());
    }
}
