package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.RefreshPricesCommand;
import com.kanokna.cart.application.dto.RefreshPricesResult;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CartApplicationServiceRefreshPricesTest {
    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-001: refresh updates all item prices")
    void refreshUpdatesAllItemPrices() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-70",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-70", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-70", Instant.now().plusSeconds(3600)),
            CartServiceTestFixture.item("T-71", "Door", "DOOR", 1, CartServiceTestFixture.money("2000.00"), "hash-71", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.queueQuote("T-70", new PricingPort.PriceQuote(
            true,
            "Q-70",
            Money.of(new BigDecimal("1100.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));
        context.pricingPort.queueQuote("T-71", new PricingPort.PriceQuote(
            true,
            "Q-71",
            Money.of(new BigDecimal("2100.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));

        RefreshPricesResult result = context.service.refreshPrices(new RefreshPricesCommand("cust-70", null));

        assertEquals(2, result.itemsUpdated());
        assertEquals("Q-70", result.cart().items().get(0).quoteId());
        assertEquals("Q-71", result.cart().items().get(1).quoteId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-002: refresh clears price_stale flags")
    void refreshClearsPriceStaleFlags() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        CartItem item = CartServiceTestFixture.item(
            "T-72",
            "Window",
            "WINDOW",
            1,
            CartServiceTestFixture.money("1000.00"),
            "hash-72",
            Instant.now().plusSeconds(3600)
        );
        item.markPriceStale(true);
        Cart cart = CartServiceTestFixture.cartWithItems("cust-71", context.totalsCalculator, item);
        context.cartRepository.save(cart);
        context.pricingPort.queueQuote("T-72", new PricingPort.PriceQuote(
            true,
            "Q-72",
            Money.of(new BigDecimal("1000.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));

        RefreshPricesResult result = context.service.refreshPrices(new RefreshPricesCommand("cust-71", null));

      assertFalse(result.cart().items().get(0).priceStale());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-003: refresh recalculates totals")
    void refreshRecalculatesTotals() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-72",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-73", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-73", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.queueQuote("T-73", new PricingPort.PriceQuote(
            true,
            "Q-73",
            Money.of(new BigDecimal("2000.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));

        RefreshPricesResult result = context.service.refreshPrices(new RefreshPricesCommand("cust-72", null));

        assertEquals(CartServiceTestFixture.money("2000.00"), result.cart().subtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-004: promo discount recalculated with new subtotal")
    void promoDiscountRecalculated() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-73",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-74", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-74", Instant.now().plusSeconds(3600))
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo("SAVE10", CartServiceTestFixture.money("100.00"));
        cart.applyPromoCode(promo, context.totalsCalculator, null);
        context.cartRepository.save(cart);
        context.pricingPort.queueQuote("T-74", new PricingPort.PriceQuote(
            true,
            "Q-74",
            Money.of(new BigDecimal("2000.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));
        context.pricingPort.setPromoResult(
            "SAVE10",
            new PricingPort.PromoValidationResult(
                true,
                true,
                Money.of(new BigDecimal("200.00"), Currency.RUB),
                null,
                null
            )
        );

        RefreshPricesResult result = context.service.refreshPrices(new RefreshPricesCommand("cust-73", null));

        assertEquals(CartServiceTestFixture.money("200.00"), result.cart().discount());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-005: pricing service timeout returns error")
    void pricingServiceTimeoutReturnsError() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-74",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-75", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-75", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.setAvailable(false);

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.refreshPrices(new RefreshPricesCommand("cust-74", null))
        );

        assertEquals("ERR-CART-PRICING-UNAVAILABLE", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-006: empty cart returns error")
    void emptyCartReturnsError() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        context.cartRepository.save(Cart.createForCustomer("cust-75", Currency.RUB));

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.refreshPrices(new RefreshPricesCommand("cust-75", null))
        );

        assertEquals("ERR-CART-EMPTY", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-007: response includes change indicators")
    void responseIncludesChangeIndicators() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-76",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-76", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-76", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.queueQuote("T-76", new PricingPort.PriceQuote(
            true,
            "Q-76",
            Money.of(new BigDecimal("1500.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        ));

        RefreshPricesResult result = context.service.refreshPrices(new RefreshPricesCommand("cust-76", null));

        assertTrue(result.totalChanged());
        assertNotNull(result.previousTotal());
        assertTrue(result.priceChangePercent() > 0.0d);
    }
}
