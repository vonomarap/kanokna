package com.kanokna.cart.application.service;

import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.port.out.PricingPort.PromoValidationResult;
import com.kanokna.cart.domain.model.*;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartPromoCodeServiceTest {

    @Mock
    private PricingPort pricingPort;

    private CartPromoCodeService promoCodeService;
    private CartTotalsCalculator totalsCalculator;

    @BeforeEach
    void setUp() {
        totalsCalculator = new CartTotalsCalculator();
        promoCodeService = new CartPromoCodeService(pricingPort, totalsCalculator);
    }

    @Test
    @DisplayName("applyPromoCode succeeds with valid code")
    void applyPromoCode_validCode_succeeds() {
        Cart cart = createCartWithItems(2);
        Money discount = Money.of(BigDecimal.valueOf(1000), Currency.RUB);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(new PromoValidationResult(true, true, discount, null, null));

        var result = promoCodeService.applyPromoCode(cart, "PROMO10");

        assertThat(result.success()).isTrue();
        assertThat(result.appliedPromoCode()).isNotNull();
        assertThat(result.appliedPromoCode().code()).isEqualTo("PROMO10");
        assertThat(result.discount()).isEqualTo(discount);
    }

    @Test
    @DisplayName("applyPromoCode fails with invalid code")
    void applyPromoCode_invalidCode_fails() {
        Cart cart = createCartWithItems(2);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(new PromoValidationResult(true, false, null, "ERR-PROMO-INVALID", "Code not found"));

        var result = promoCodeService.applyPromoCode(cart, "INVALID");

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("ERR-CART-PROMO-INVALID");
    }

    @Test
    @DisplayName("applyPromoCode fails when pricing service unavailable")
    void applyPromoCode_unavailable_fails() {
        Cart cart = createCartWithItems(2);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(PromoValidationResult.unavailable());

        var result = promoCodeService.applyPromoCode(cart, "PROMO10");

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("ERR-CART-PRICING-UNAVAILABLE");
    }

    @Test
    @DisplayName("applyPromoCode fails when minimum not met")
    void applyPromoCode_minimumNotMet_fails() {
        Cart cart = createCartWithItems(1);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(new PromoValidationResult(true, false, null, "ERR-PROMO-MIN-SUBTOTAL", "Minimum not met"));

        var result = promoCodeService.applyPromoCode(cart, "MIN50K");

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("ERR-CART-PROMO-MIN-NOT-MET");
    }

    @Test
    @DisplayName("removePromoCode removes existing promo")
    void removePromoCode_existingPromo_removes() {
        AppliedPromoCode promo = new AppliedPromoCode(
            "PROMO10",
            Money.of(BigDecimal.valueOf(1000), Currency.RUB),
            "10% off",
            Instant.now()
        );
        Cart cart = createCartWithPromo(promo);

        AppliedPromoCode removed = promoCodeService.removePromoCode(cart);

        assertThat(removed).isNotNull();
        assertThat(removed.code()).isEqualTo("PROMO10");
    }

    @Test
    @DisplayName("removePromoCode returns null when no promo applied")
    void removePromoCode_noPromo_returnsNull() {
        Cart cart = createCartWithItems(2);

        AppliedPromoCode removed = promoCodeService.removePromoCode(cart);

        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("validatePromoCode returns valid for valid code")
    void validatePromoCode_validCode_returnsValid() {
        Money subtotal = Money.of(BigDecimal.valueOf(20000), Currency.RUB);
        Money discount = Money.of(BigDecimal.valueOf(2000), Currency.RUB);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(new PromoValidationResult(true, true, discount, null, null));

        var result = promoCodeService.validatePromoCode("PROMO10", subtotal);

        assertThat(result.available()).isTrue();
        assertThat(result.valid()).isTrue();
        assertThat(result.discountAmount()).isEqualTo(discount);
    }

    @Test
    @DisplayName("validatePromoCode returns unavailable when service down")
    void validatePromoCode_unavailable_returnsUnavailable() {
        Money subtotal = Money.of(BigDecimal.valueOf(20000), Currency.RUB);

        when(pricingPort.validatePromoCode(anyString(), any()))
            .thenReturn(PromoValidationResult.unavailable());

        var result = promoCodeService.validatePromoCode("PROMO10", subtotal);

        assertThat(result.available()).isFalse();
    }

    private Cart createCartWithItems(int itemCount) {
        List<CartItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(CartItem.create(
                "PRODUCT-" + i,
                "Product " + i,
                "WINDOW",
                new ConfigurationSnapshot("WINDOW-001", 1200, 1500, List.of(), List.of()),
                "hash-" + i,
                1,
                Money.of(BigDecimal.valueOf(10000), Currency.RUB),
                new PriceQuoteReference("quote-" + i, Instant.now().plusSeconds(3600)),
                ValidationStatus.VALID,
                null,
                null,
                false,
                Instant.now()
            ));
        }

        Money subtotal = Money.of(BigDecimal.valueOf(10000 * itemCount), Currency.RUB);
        return Cart.rehydrate(
            CartId.generate(),
            "customer-1",
            null,
            CartStatus.ACTIVE,
            null,
            new CartTotals(subtotal, Money.zero(Currency.RUB), Money.zero(Currency.RUB), subtotal, itemCount),
            items,
            Instant.now(),
            Instant.now(),
            0
        );
    }

    private Cart createCartWithPromo(AppliedPromoCode promo) {
        List<CartItem> items = new java.util.ArrayList<>();
        items.add(CartItem.create(
            "PRODUCT-1",
            "Product 1",
            "WINDOW",
            new ConfigurationSnapshot("WINDOW-001", 1200, 1500, List.of(), List.of()),
            "hash-1",
            1,
            Money.of(BigDecimal.valueOf(10000), Currency.RUB),
            new PriceQuoteReference("quote-1", Instant.now().plusSeconds(3600)),
            ValidationStatus.VALID,
            null,
            null,
            false,
            Instant.now()
        ));

        Money subtotal = Money.of(BigDecimal.valueOf(10000), Currency.RUB);
        Money total = subtotal.subtract(promo.discountAmount());

        return Cart.rehydrate(
            CartId.generate(),
            "customer-1",
            null,
            CartStatus.ACTIVE,
            promo,
            new CartTotals(subtotal, promo.discountAmount(), Money.zero(Currency.RUB), total, 1),
            items,
            Instant.now(),
            Instant.now(),
            0
        );
    }
}
