package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceSnapshotTest {
    @Test
    @DisplayName("TC-FUNC-CART-SNAP-001: Snapshot created with fresh prices")
    void snapshotCreatedWithFreshPrices() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-snap-1",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-90",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-90",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        CreateSnapshotResult result = context.service.createSnapshot(
            new CreateSnapshotCommand("cust-snap-1", false)
        );

        assertNotNull(result.snapshotId());
        assertNotNull(result.cartSnapshot());
        assertTrue(!result.pricesChanged());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-002: Original cart cleared after snapshot")
    void cartMarkedCheckedOutAfterSnapshot() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-snap-2",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-91",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-91",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        context.service.createSnapshot(new CreateSnapshotCommand("cust-snap-2", true));

        Cart updated = context.cartRepository.findByCustomerId("cust-snap-2").orElse(null);
        assertNotNull(updated);
        assertEquals(CartStatus.CHECKED_OUT, updated.status());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-003: Invalid items prevent snapshot creation")
    void invalidItemsPreventSnapshotCreation() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.catalogPort.setValid(false);
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-snap-3",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-92",
                "Door",
                "DOOR",
                1,
                CartServiceTestFixture.money("1200.00"),
                "hash-92",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.createSnapshot(new CreateSnapshotCommand("cust-snap-3", true))
        );

        assertEquals("ERR-CART-INVALID-ITEMS", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-004: Anonymous cart rejected")
    void anonymousCartRejected() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.createSnapshot(new CreateSnapshotCommand(null, true))
        );

        assertEquals("ERR-CART-ANONYMOUS", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-005: Snapshot has validity period")
    void snapshotHasValidityPeriod() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-snap-4",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-93",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-93",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(cart);

        Instant start = Instant.now();
        CreateSnapshotResult result = context.service.createSnapshot(
            new CreateSnapshotCommand("cust-snap-4", true)
        );

        Instant earliest = start.plus(Duration.ofMinutes(14));
        Instant latest = start.plus(Duration.ofMinutes(16));
        assertTrue(result.validUntil().isAfter(earliest));
        assertTrue(result.validUntil().isBefore(latest));
    }
}
