package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.RemovePromoCodeCommand;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.support.CartServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartApplicationServicePromoCodeTest {
    @Test
    @DisplayName("TC-FUNC-CART-PROMO-001: valid promo applies discount")
    void validPromoAppliesDiscount() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-50",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-50", "Window", "WINDOW", 1, CartServiceTestFixture.money("10000.00"), "hash-50", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.setPromoResult(
            "SAVE10",
            new com.kanokna.cart.application.port.out.PricingPort.PromoValidationResult(
                true,
                true,
                Money.of(new BigDecimal("1000.00"), Currency.RUB),
                null,
                null
            )
        );

        ApplyPromoCodeResult result = context.service.applyPromoCode(new ApplyPromoCodeCommand("cust-50", null, "SAVE10"));

        assertTrue(result.applied());
        assertEquals(CartServiceTestFixture.money("1000.00"), result.cart().discount());
        assertEquals(CartServiceTestFixture.money("9000.00"), result.cart().total());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-002: invalid promo returns error code")
    void invalidPromoReturnsErrorCode() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-51",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-51", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-51", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.setPromoResult(
            "INVALID",
            new com.kanokna.cart.application.port.out.PricingPort.PromoValidationResult(
                true,
                false,
                Money.zero(Currency.RUB),
                "ERR-PROMO-INVALID",
                "Invalid"
            )
        );

        ApplyPromoCodeResult result = context.service.applyPromoCode(new ApplyPromoCodeCommand("cust-51", null, "INVALID"));

        assertEquals("ERR-CART-PROMO-INVALID", result.errorCode());
        assertTrue(!result.applied());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-003: new promo replaces existing")
    void newPromoReplacesExisting() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-52",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-52", "Window", "WINDOW", 1, CartServiceTestFixture.money("10000.00"), "hash-52", Instant.now().plusSeconds(3600))
        );
        AppliedPromoCode existing = CartServiceTestFixture.promo("OLD10", CartServiceTestFixture.money("500.00"));
        cart.applyPromoCode(existing, context.totalsCalculator, null);
        context.cartRepository.save(cart);
        context.pricingPort.setPromoResult(
            "NEW20",
            new com.kanokna.cart.application.port.out.PricingPort.PromoValidationResult(
                true,
                true,
                Money.of(new BigDecimal("2000.00"), Currency.RUB),
                null,
                null
            )
        );

        ApplyPromoCodeResult result = context.service.applyPromoCode(new ApplyPromoCodeCommand("cust-52", null, "NEW20"));

        assertEquals("NEW20", result.cart().appliedPromoCode().code());
        assertEquals(CartServiceTestFixture.money("2000.00"), result.cart().discount());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-004: empty cart rejects promo")
    void emptyCartRejectsPromo() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = Cart.createForCustomer("cust-53", Currency.RUB);
        context.cartRepository.save(cart);

        DomainException ex = assertThrows(DomainException.class, () ->
            context.service.applyPromoCode(new ApplyPromoCodeCommand("cust-53", null, "SAVE10"))
        );

        assertEquals("ERR-CART-EMPTY", ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-005: minimum order requirement checked")
    void minimumOrderRequirementChecked() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-54",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-54", "Window", "WINDOW", 1, CartServiceTestFixture.money("1000.00"), "hash-54", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);
        context.pricingPort.setPromoResult(
            "MIN5000",
            new com.kanokna.cart.application.port.out.PricingPort.PromoValidationResult(
                true,
                false,
                Money.zero(Currency.RUB),
                "ERR-PROMO-MIN-SUBTOTAL",
                "Minimum not met"
            )
        );

        ApplyPromoCodeResult result = context.service.applyPromoCode(new ApplyPromoCodeCommand("cust-54", null, "MIN5000"));

        assertEquals("ERR-CART-PROMO-MIN-NOT-MET", result.errorCode());
        assertTrue(!result.applied());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-REMOVE-001: remove promo zeroes discount")
    void removePromoZeroesDiscount() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-55",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-55", "Window", "WINDOW", 1, CartServiceTestFixture.money("10000.00"), "hash-55", Instant.now().plusSeconds(3600))
        );
        AppliedPromoCode promo = CartServiceTestFixture.promo("SAVE10", CartServiceTestFixture.money("1000.00"));
        cart.applyPromoCode(promo, context.totalsCalculator, null);
        context.cartRepository.save(cart);

        CartDto result = context.service.removePromoCode(new RemovePromoCodeCommand("cust-55", null));

        assertEquals(CartServiceTestFixture.money("0.00"), result.discount());
        assertEquals(result.subtotal(), result.total());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-REMOVE-002: no promo returns unchanged cart")
    void noPromoReturnsUnchangedCart() {
        CartServiceTestFixture.TestContext context = new CartServiceTestFixture.TestContext();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-56",
            context.totalsCalculator,
            CartServiceTestFixture.item("T-56", "Window", "WINDOW", 1, CartServiceTestFixture.money("500.00"), "hash-56", Instant.now().plusSeconds(3600))
        );
        context.cartRepository.save(cart);

        CartDto result = context.service.removePromoCode(new RemovePromoCodeCommand("cust-56", null));

        assertNull(result.appliedPromoCode());
        assertEquals(CartServiceTestFixture.money("500.00"), result.subtotal());
    }
}
