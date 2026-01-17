package com.kanokna.cart.application.service;

import com.kanokna.cart.domain.model.*;
import com.kanokna.cart.domain.service.CartMergeService;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartMergingServiceTest {

    @Mock
    private CartMergeService domainMergeService;

    @Mock
    private CartTotalsCalculator totalsCalculator;

    private CartMergingService mergingService;

    @BeforeEach
    void setUp() {
        mergingService = new CartMergingService(domainMergeService, totalsCalculator);
    }

    @Test
    @DisplayName("mergeCarts delegates to domain service and returns result")
    void mergeCarts_validCarts_returnsMergeResult() {
        Cart source = createCart("session-1", null, 2);
        Cart target = createCart(null, "customer-1", 1);

        when(domainMergeService.merge(any(), any(), any()))
            .thenReturn(new CartMergeService.MergeResult(2, 1, 1, false, "NONE"));

        var result = mergingService.mergeCarts(source, target);

        assertThat(result.itemsFromSource()).isEqualTo(2);
        assertThat(result.itemsMerged()).isEqualTo(1);
        assertThat(result.itemsAdded()).isEqualTo(1);
        assertThat(result.totalItemsTransferred()).isEqualTo(2);
    }

    @Test
    @DisplayName("mergeCarts preserves promo code from authenticated cart")
    void mergeCarts_withPromo_preservesAuthenticatedPromo() {
        Cart source = createCart("session-1", null, 1);
        Cart target = createCart(null, "customer-1", 1);

        when(domainMergeService.merge(any(), any(), any()))
            .thenReturn(new CartMergeService.MergeResult(1, 0, 1, true, "AUTHENTICATED"));

        var result = mergingService.mergeCarts(source, target);

        assertThat(result.promoCodePreserved()).isTrue();
        assertThat(result.promoCodeSource()).isEqualTo("AUTHENTICATED");
    }

    @Test
    @DisplayName("canMerge returns false for null carts")
    void canMerge_nullCarts_returnsFalse() {
        Cart cart = createCart(null, "customer-1", 1);

        assertThat(mergingService.canMerge(null, cart)).isFalse();
        assertThat(mergingService.canMerge(cart, null)).isFalse();
    }

    @Test
    @DisplayName("canMerge returns false for same cart")
    void canMerge_sameCart_returnsFalse() {
        Cart cart = createCart(null, "customer-1", 1);

        assertThat(mergingService.canMerge(cart, cart)).isFalse();
    }

    @Test
    @DisplayName("canMerge returns false for empty source")
    void canMerge_emptySource_returnsFalse() {
        Cart source = createCart("session-1", null, 0);
        Cart target = createCart(null, "customer-1", 1);

        assertThat(mergingService.canMerge(source, target)).isFalse();
    }

    @Test
    @DisplayName("canMerge returns true for valid merge")
    void canMerge_validMerge_returnsTrue() {
        Cart source = createCart("session-1", null, 2);
        Cart target = createCart(null, "customer-1", 1);

        assertThat(mergingService.canMerge(source, target)).isTrue();
    }

    @Test
    @DisplayName("findMatchingItem returns true when item exists")
    void findMatchingItem_exists_returnsTrue() {
        Cart target = createCart(null, "customer-1", 1);
        String hash = target.items().getFirst().configurationHash();

        assertThat(mergingService.findMatchingItem(target, hash)).isTrue();
    }

    @Test
    @DisplayName("findMatchingItem returns false when item not found")
    void findMatchingItem_notFound_returnsFalse() {
        Cart target = createCart(null, "customer-1", 1);

        assertThat(mergingService.findMatchingItem(target, "non-existent-hash")).isFalse();
    }

    private Cart createCart(String sessionId, String customerId, int itemCount) {
        List<CartItem> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(createCartItem("hash-" + i + "-" + System.nanoTime()));
        }

        Money subtotal = items.stream()
            .map(CartItem::lineTotal)
            .reduce(Money.zero(Currency.RUB), Money::add);

        return Cart.rehydrate(
            CartId.generate(),
            customerId,
            sessionId,
            CartStatus.ACTIVE,
            null,
            new CartTotals(subtotal, Money.zero(Currency.RUB), Money.zero(Currency.RUB), subtotal, items.size()),
            items,
            Instant.now(),
            Instant.now(),
            0
        );
    }

    private CartItem createCartItem(String hash) {
        return CartItem.create(
            "PRODUCT-1",
            "Test Product",
            "WINDOW",
            new ConfigurationSnapshot("WINDOW-001", 1200, 1500, List.of(), List.of()),
            hash,
            1,
            Money.of(BigDecimal.valueOf(10000), Currency.RUB),
            new PriceQuoteReference("quote-1", Instant.now().plusSeconds(3600)),
            ValidationStatus.VALID,
            null,
            null,
            false,
            Instant.now()
        );
    }
}
