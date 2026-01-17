package com.kanokna.cart.application.service;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.port.out.PricingPort.PriceQuote;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartPricingServiceTest {

    @Mock
    private PricingPort pricingPort;

    @Mock
    private CartTotalsCalculator totalsCalculator;

    private CartProperties properties;
    private CartPricingService pricingService;

    @BeforeEach
    void setUp() {
        properties = new CartProperties();
        properties.setPriceChangeThresholdPercent(1.0);
        pricingService = new CartPricingService(pricingPort, totalsCalculator, properties);
    }

    @Test
    @DisplayName("fetchQuote returns success when pricing service available")
    void fetchQuote_available_returnsSuccess() {
        ConfigurationSnapshot snapshot = createSnapshot();
        Instant validUntil = Instant.now().plusSeconds(3600);
        Money price = Money.of(BigDecimal.valueOf(15000), Currency.RUB);

        when(pricingPort.calculateQuote(any(), any()))
            .thenReturn(new PriceQuote(true, "quote-123", price, validUntil));

        var result = pricingService.fetchQuote(snapshot, Currency.RUB);

        assertThat(result.available()).isTrue();
        assertThat(result.quoteId()).isEqualTo("quote-123");
        assertThat(result.unitPrice()).isEqualTo(price);
        assertThat(result.validUntil()).isEqualTo(validUntil);
    }

    @Test
    @DisplayName("fetchQuote returns unavailable when pricing service down")
    void fetchQuote_unavailable_returnsUnavailable() {
        ConfigurationSnapshot snapshot = createSnapshot();
        when(pricingPort.calculateQuote(any(), any()))
            .thenReturn(PriceQuote.unavailable());

        var result = pricingService.fetchQuote(snapshot, Currency.RUB);

        assertThat(result.available()).isFalse();
    }

    @Test
    @DisplayName("fetchQuote returns expired when quote already expired")
    void fetchQuote_expiredQuote_returnsExpired() {
        ConfigurationSnapshot snapshot = createSnapshot();
        Instant expiredTime = Instant.now().minusSeconds(60);
        Money price = Money.of(BigDecimal.valueOf(15000), Currency.RUB);

        when(pricingPort.calculateQuote(any(), any()))
            .thenReturn(new PriceQuote(true, "quote-expired", price, expiredTime));

        var result = pricingService.fetchQuote(snapshot, Currency.RUB);

        assertThat(result.available()).isFalse();
        assertThat(result.expired()).isTrue();
        assertThat(result.quoteId()).isEqualTo("quote-expired");
    }

    @Test
    @DisplayName("refreshAllPrices updates all item prices")
    void refreshAllPrices_multipleItems_updatesAll() {
        Cart cart = createCartWithItems(2);
        Money oldTotal = cart.totals().total();
        Money newPrice = Money.of(BigDecimal.valueOf(20000), Currency.RUB);
        Instant validUntil = Instant.now().plusSeconds(3600);

        when(pricingPort.calculateQuote(any(), any()))
            .thenReturn(new PriceQuote(true, "quote-new", newPrice, validUntil));
        when(totalsCalculator.calculateTotals(any(), any(), any()))
            .thenReturn(new CartTotals(
                Money.of(BigDecimal.valueOf(40000), Currency.RUB),
                Money.zero(Currency.RUB),
                Money.zero(Currency.RUB),
                Money.of(BigDecimal.valueOf(40000), Currency.RUB),
                2
            ));

        var result = pricingService.refreshAllPrices(cart);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failCount()).isZero();
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    @DisplayName("refreshAllPrices reports partial failure")
    void refreshAllPrices_partialFailure_reportsPartial() {
        Cart cart = createCartWithItems(2);

        when(pricingPort.calculateQuote(any(), any()))
            .thenReturn(new PriceQuote(true, "quote-1",
                Money.of(BigDecimal.valueOf(20000), Currency.RUB),
                Instant.now().plusSeconds(3600)))
            .thenReturn(PriceQuote.unavailable());
        when(totalsCalculator.calculateTotals(any(), any(), any()))
            .thenReturn(new CartTotals(
                Money.of(BigDecimal.valueOf(20000), Currency.RUB),
                Money.zero(Currency.RUB),
                Money.zero(Currency.RUB),
                Money.of(BigDecimal.valueOf(20000), Currency.RUB),
                1
            ));

        var result = pricingService.refreshAllPrices(cart);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
        assertThat(result.isPartial()).isTrue();
    }

    @Test
    @DisplayName("isPriceChangeSignificant returns true when change exceeds threshold")
    void isPriceChangeSignificant_exceedsThreshold_returnsTrue() {
        Money previous = Money.of(BigDecimal.valueOf(10000), Currency.RUB);
        Money newTotal = Money.of(BigDecimal.valueOf(10200), Currency.RUB); // 2% change

        boolean significant = pricingService.isPriceChangeSignificant(previous, newTotal);

        assertThat(significant).isTrue();
    }

    @Test
    @DisplayName("isPriceChangeSignificant returns false when change below threshold")
    void isPriceChangeSignificant_belowThreshold_returnsFalse() {
        Money previous = Money.of(BigDecimal.valueOf(10000), Currency.RUB);
        Money newTotal = Money.of(BigDecimal.valueOf(10050), Currency.RUB); // 0.5% change

        boolean significant = pricingService.isPriceChangeSignificant(previous, newTotal);

        assertThat(significant).isFalse();
    }

    @Test
    @DisplayName("checkPriceStaleness returns true when any quote is stale")
    void checkPriceStaleness_staleQuote_returnsTrue() {
        List<CartItem> items = List.of(
            createCartItem("quote-1", Instant.now().minusSeconds(60)) // Expired
        );
        Cart cart = createCartWithSpecificItems(items);

        boolean stale = pricingService.checkPriceStaleness(cart);

        assertThat(stale).isTrue();
    }

    private ConfigurationSnapshot createSnapshot() {
        return new ConfigurationSnapshot(
            "WINDOW-001",
            1200,
            1500,
            List.of(),
            List.of()
        );
    }

    private CartItem createCartItem(String quoteId, Instant validUntil) {
        return CartItem.create(
            "PRODUCT-1",
            "Test Product",
            "WINDOW",
            createSnapshot(),
            "hash-1",
            1,
            Money.of(BigDecimal.valueOf(10000), Currency.RUB),
            new PriceQuoteReference(quoteId, validUntil),
            ValidationStatus.VALID,
            null,
            null,
            false,
            Instant.now()
        );
    }

    private Cart createCartWithItems(int itemCount) {
        List<CartItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(createCartItem("quote-" + i, Instant.now().plusSeconds(3600)));
        }
        return createCartWithSpecificItems(items);
    }

    private Cart createCartWithSpecificItems(List<CartItem> items) {
        Money subtotal = items.stream()
            .map(CartItem::lineTotal)
            .reduce(Money.zero(Currency.RUB), Money::add);

        return Cart.rehydrate(
            CartId.generate(),
            "customer-1",
            null,
            CartStatus.ACTIVE,
            null,
            new CartTotals(subtotal, Money.zero(Currency.RUB), Money.zero(Currency.RUB), subtotal, items.size()),
            items,
            Instant.now(),
            Instant.now(),
            0
        );
    }
}
