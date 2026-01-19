package com.kanokna.cart.application.service;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.service.dto.PriceRefreshResult;
import com.kanokna.cart.application.service.dto.ValidationResult;
import com.kanokna.cart.domain.model.*;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartCheckoutServiceTest {

    @Mock
    private CartItemValidationService validationService;

    @Mock
    private CartPricingService pricingService;

    private CartProperties properties;
    private CartCheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        properties = new CartProperties(null, null, null, null);
        checkoutService = new CartCheckoutService(validationService, pricingService, properties);
    }

    @Test
    @DisplayName("createSnapshot succeeds with valid cart")
    void createSnapshot_validCart_succeeds() {
        Cart cart = createCartWithItems(2);
        Money total = cart.totals().total();

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult());
        when(pricingService.refreshAllPrices(any()))
            .thenReturn(new PriceRefreshResult(total, total, 0.0, 0, 2, 0, false));

        var result = checkoutService.createSnapshot(cart, false);

        assertThat(result.success()).isTrue();
        assertThat(result.snapshot()).isNotNull();
        assertThat(result.snapshot().items()).hasSize(2);
    }

    @Test
    @DisplayName("createSnapshot fails with invalid items")
    void createSnapshot_invalidItems_fails() {
        Cart cart = createCartWithItems(2);

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult())
            .thenReturn(ValidationResult.invalid(List.of("Error")));

        var result = checkoutService.createSnapshot(cart, false);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason())
            .isEqualTo(CartCheckoutService.SnapshotCreationResult.FailureReason.INVALID_ITEMS);
    }

    @Test
    @DisplayName("createSnapshot fails when pricing unavailable")
    void createSnapshot_pricingFailed_fails() {
        Cart cart = createCartWithItems(2);
        Money total = cart.totals().total();

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult());
        when(pricingService.refreshAllPrices(any()))
            .thenReturn(new PriceRefreshResult(total, total, 0.0, 0, 0, 2, false));

        var result = checkoutService.createSnapshot(cart, false);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason())
            .isEqualTo(CartCheckoutService.SnapshotCreationResult.FailureReason.PRICING_FAILED);
    }

    @Test
    @DisplayName("createSnapshot requires acknowledgment for significant price change")
    void createSnapshot_significantPriceChange_requiresAcknowledgment() {
        Cart cart = createCartWithItems(2);
        Money oldTotal = cart.totals().total();
        Money newTotal = Money.of(oldTotal.getAmount().multiply(BigDecimal.valueOf(1.05)), Currency.RUB);

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult());
        when(pricingService.refreshAllPrices(any()))
            .thenReturn(new PriceRefreshResult(oldTotal, newTotal, 5.0, 2, 2, 0, true));

        var result = checkoutService.createSnapshot(cart, false);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason())
            .isEqualTo(CartCheckoutService.SnapshotCreationResult.FailureReason.REQUIRES_ACKNOWLEDGMENT);
    }

    @Test
    @DisplayName("createSnapshot succeeds when price change acknowledged")
    void createSnapshot_priceChangeAcknowledged_succeeds() {
        Cart cart = createCartWithItems(2);
        Money oldTotal = cart.totals().total();
        Money newTotal = Money.of(oldTotal.getAmount().multiply(BigDecimal.valueOf(1.05)), Currency.RUB);

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult());
        when(pricingService.refreshAllPrices(any()))
            .thenReturn(new PriceRefreshResult(oldTotal, newTotal, 5.0, 2, 2, 0, true));

        var result = checkoutService.createSnapshot(cart, true);

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("validateForCheckout returns valid when all items valid")
    void validateForCheckout_allValid_returnsValid() {
        Cart cart = createCartWithItems(3);

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult());

        var result = checkoutService.validateForCheckout(cart);

        assertThat(result.valid()).isTrue();
        assertThat(result.validItemCount()).isEqualTo(3);
        assertThat(result.invalidItemCount()).isZero();
    }

    @Test
    @DisplayName("validateForCheckout reports invalid items")
    void validateForCheckout_someInvalid_reportsInvalid() {
        Cart cart = createCartWithItems(3);

        when(validationService.validateConfiguration(any()))
            .thenReturn(ValidationResult.validResult())
            .thenReturn(ValidationResult.invalid(List.of("Error")))
            .thenReturn(ValidationResult.validResult());

        var result = checkoutService.validateForCheckout(cart);

        assertThat(result.valid()).isFalse();
        assertThat(result.validItemCount()).isEqualTo(2);
        assertThat(result.invalidItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("requiresAcknowledgement returns false when no change")
    void requiresAcknowledgement_noChange_returnsFalse() {
        Money total = Money.of(BigDecimal.valueOf(10000), Currency.RUB);
        PriceRefreshResult result = new PriceRefreshResult(total, total, 0.0, 0, 2, 0, false);

        assertThat(checkoutService.requiresAcknowledgement(result)).isFalse();
    }

    @Test
    @DisplayName("requiresAcknowledgement returns true when change exceeds threshold")
    void requiresAcknowledgement_exceedsThreshold_returnsTrue() {
        Money oldTotal = Money.of(BigDecimal.valueOf(10000), Currency.RUB);
        Money newTotal = Money.of(BigDecimal.valueOf(10500), Currency.RUB);
        PriceRefreshResult result = new PriceRefreshResult(oldTotal, newTotal, 5.0, 2, 2, 0, true);

        assertThat(checkoutService.requiresAcknowledgement(result)).isTrue();
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
}
