package com.kanokna.cart.application.service;

import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort.ValidationResult;
import com.kanokna.cart.domain.model.*;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartItemValidationServiceTest {

    @Mock
    private CatalogConfigurationPort catalogPort;

    private CartItemValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new CartItemValidationService(catalogPort);
    }

    @Test
    @DisplayName("validateConfiguration returns valid when catalog confirms valid")
    void validateConfiguration_validConfiguration_returnsValid() {
        ConfigurationSnapshot snapshot = createSnapshot();
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(new ValidationResult(true, true, List.of(), List.of()));

        var result = validationService.validateConfiguration(snapshot);

        assertThat(result.available()).isTrue();
        assertThat(result.valid()).isTrue();
        assertThat(result.status()).isEqualTo(ValidationStatus.VALID);
    }

    @Test
    @DisplayName("validateConfiguration returns invalid when catalog finds errors")
    void validateConfiguration_invalidConfiguration_returnsInvalid() {
        ConfigurationSnapshot snapshot = createSnapshot();
        List<String> errors = List.of("Width out of range", "Invalid option combination");
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(new ValidationResult(true, false, errors, List.of()));

        var result = validationService.validateConfiguration(snapshot);

        assertThat(result.available()).isTrue();
        assertThat(result.valid()).isFalse();
        assertThat(result.status()).isEqualTo(ValidationStatus.INVALID);
        assertThat(result.errors()).containsExactlyElementsOf(errors);
    }

    @Test
    @DisplayName("validateConfiguration returns unavailable when catalog is down")
    void validateConfiguration_catalogUnavailable_returnsUnavailable() {
        ConfigurationSnapshot snapshot = createSnapshot();
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(ValidationResult.unavailable());

        var result = validationService.validateConfiguration(snapshot);

        assertThat(result.available()).isFalse();
        assertThat(result.status()).isEqualTo(ValidationStatus.UNKNOWN);
    }

    @Test
    @DisplayName("validateConfiguration handles exception gracefully")
    void validateConfiguration_exception_returnsUnavailable() {
        ConfigurationSnapshot snapshot = createSnapshot();
        when(catalogPort.validateConfiguration(any()))
            .thenThrow(new RuntimeException("Connection failed"));

        var result = validationService.validateConfiguration(snapshot);

        assertThat(result.available()).isFalse();
        assertThat(result.status()).isEqualTo(ValidationStatus.UNKNOWN);
    }

    @Test
    @DisplayName("revalidateCartItems validates all items in cart")
    void revalidateCartItems_multipleItems_validatesAll() {
        Cart cart = createCartWithItems(3);
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(new ValidationResult(true, true, List.of(), List.of()));

        var summary = validationService.revalidateCartItems(cart);

        assertThat(summary.totalCount()).isEqualTo(3);
        assertThat(summary.validCount()).isEqualTo(3);
        assertThat(summary.invalidCount()).isZero();
        assertThat(summary.allValid()).isTrue();
    }

    @Test
    @DisplayName("hasInvalidItems returns true when invalid item exists")
    void hasInvalidItems_withInvalidItem_returnsTrue() {
        Cart cart = createCartWithItems(2);
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(new ValidationResult(true, true, List.of(), List.of()))
            .thenReturn(new ValidationResult(true, false, List.of("Error"), List.of()));

        boolean hasInvalid = validationService.hasInvalidItems(cart);

        assertThat(hasInvalid).isTrue();
    }

    @Test
    @DisplayName("countInvalidItems returns correct count")
    void countInvalidItems_mixedValidity_returnsCorrectCount() {
        Cart cart = createCartWithItems(3);
        when(catalogPort.validateConfiguration(any()))
            .thenReturn(new ValidationResult(true, true, List.of(), List.of()))
            .thenReturn(new ValidationResult(true, false, List.of("Error"), List.of()))
            .thenReturn(new ValidationResult(true, false, List.of("Error"), List.of()));

        int count = validationService.countInvalidItems(cart);

        assertThat(count).isEqualTo(2);
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

    private Cart createCartWithItems(int itemCount) {
        List<CartItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(CartItem.create(
                "PRODUCT-" + i,
                "Product " + i,
                "WINDOW",
                createSnapshot(),
                "hash-" + i,
                1,
                Money.of(java.math.BigDecimal.valueOf(10000), Currency.RUB),
                new PriceQuoteReference("quote-" + i, Instant.now().plusSeconds(3600)),
                ValidationStatus.VALID,
                null,
                null,
                false,
                Instant.now()
            ));
        }

        return Cart.rehydrate(
            CartId.generate(),
            "customer-1",
            null,
            CartStatus.ACTIVE,
            null,
            CartTotals.empty(Currency.RUB),
            items,
            Instant.now(),
            Instant.now(),
            0
        );
    }
}
