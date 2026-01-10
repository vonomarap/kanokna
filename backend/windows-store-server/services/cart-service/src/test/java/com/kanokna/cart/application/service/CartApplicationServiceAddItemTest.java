package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.domain.event.CartItemAddedEvent;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServiceAddItemTest {
    @Test
    @DisplayName("TC-FUNC-CART-ADD-001: add valid item creates cart item with snapshot")
    void addValidItemCreatesSnapshot() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        AddItemCommand command = CartServiceTestFixture.addItemCommand(
            "cust-20",
            null,
            "T-20",
            "Window",
            "WINDOW",
            1,
            null
        );

        AddItemResult result = context.service.addItem(command);

        assertNotNull(result.addedItemId());
        assertEquals(1, result.cart().items().size());
        assertTrue(result.cart().items().get(0).configurationHash().length() > 0);
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-002: add item to existing cart appends item")
    void addItemAppendsForDifferentConfig() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart existing = CartServiceTestFixture.cartWithItems(
            "cust-21",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-21",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-21",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(existing);

        AddItemResult result = context.service.addItem(CartServiceTestFixture.addItemCommand(
            "cust-21",
            null,
            "T-22",
            "Door",
            "DOOR",
            1,
            null
        ));

        assertEquals(2, result.cart().items().size());
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-003: invalid configuration rejected")
    void invalidConfigurationRejected() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.catalogPort.setValid(false);
        AddItemCommand command = CartServiceTestFixture.addItemCommand(
            "cust-22",
            null,
            "T-23",
            "Window",
            "WINDOW",
            1,
            null
        );

        DomainException ex = assertThrows(DomainException.class, () -> context.service.addItem(command));

        assertEquals("ERR-CART-INVALID-CONFIG", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-004: expired quote triggers fresh price fetch")
    void expiredQuoteTriggersFreshFetch() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.pricingPort.queueQuote(
            "T-24",
            new com.kanokna.cart.application.port.out.PricingPort.PriceQuote(
                true,
                "QUOTE-FRESH",
                Money.of(new java.math.BigDecimal("1200.00"), Currency.RUB),
                Instant.now().plusSeconds(3600)
            )
        );
        AddItemCommand command = CartServiceTestFixture.addItemCommand(
            "cust-23",
            null,
            "T-24",
            "Window",
            "WINDOW",
            1,
            "EXPIRED-QUOTE"
        );

        AddItemResult result = context.service.addItem(command);

        assertEquals("QUOTE-FRESH", result.cart().items().get(0).quoteId());
        assertTrue(context.pricingPort.calculateQuoteCalls() > 0);
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-005: totals recalculated after add")
    void totalsRecalculatedAfterAdd() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart existing = CartServiceTestFixture.cartWithItems(
            "cust-24",
            context.totalsCalculator,
            CartServiceTestFixture.item(
                "T-25",
                "Window",
                "WINDOW",
                1,
                CartServiceTestFixture.money("1000.00"),
                "hash-25",
                Instant.now().plusSeconds(3600)
            )
        );
        context.cartRepository.save(existing);

        AddItemResult result = context.service.addItem(CartServiceTestFixture.addItemCommand(
            "cust-24",
            null,
            "T-26",
            "Door",
            "DOOR",
            1,
            null
        ));

        assertEquals(CartServiceTestFixture.money("2000.00"), result.cart().subtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-006: item added event published")
    void itemAddedEventPublished() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();

        context.service.addItem(CartServiceTestFixture.addItemCommand(
            "cust-25",
            null,
            "T-27",
            "Window",
            "WINDOW",
            1,
            null
        ));

        boolean published = context.eventPublisher.events().stream()
            .anyMatch(event -> "cart.item.added".equals(event.topic()) && event.event() instanceof CartItemAddedEvent);
        assertTrue(published);
    }
}
