package com.kanokna.cart.domain.model;

import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root for shopping cart.
 */
public class Cart {
    private final CartId cartId;
    private final String customerId;
    private final String sessionId;
    private CartStatus status;
    private AppliedPromoCode appliedPromoCode;
    private CartTotals totals;
    private final List<CartItem> items;
    private final Instant createdAt;
    private Instant updatedAt;
    private int version;

    private Cart(CartId cartId,
                 String customerId,
                 String sessionId,
                 CartStatus status,
                 AppliedPromoCode appliedPromoCode,
                 CartTotals totals,
                 List<CartItem> items,
                 Instant createdAt,
                 Instant updatedAt,
                 int version) {
        this.cartId = Objects.requireNonNull(cartId, "cartId cannot be null");
        this.customerId = customerId;
        this.sessionId = sessionId;
        if ((customerId == null || customerId.isBlank())
            && (sessionId == null || sessionId.isBlank())) {
            throw new IllegalArgumentException("cart must have customerId or sessionId");
        }
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.appliedPromoCode = appliedPromoCode;
        this.totals = Objects.requireNonNull(totals, "totals cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items cannot be null"));
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
        this.version = version;
    }

    public static Cart createForCustomer(String customerId, Currency currency) {
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Currency resolved = currency == null ? Currency.RUB : currency;
        Instant now = Instant.now();
        return new Cart(
            CartId.generate(),
            customerId,
            null,
            CartStatus.ACTIVE,
            null,
            CartTotals.empty(resolved),
            List.of(),
            now,
            now,
            0
        );
    }

    public static Cart createForSession(String sessionId, Currency currency) {
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        Currency resolved = currency == null ? Currency.RUB : currency;
        Instant now = Instant.now();
        return new Cart(
            CartId.generate(),
            null,
            sessionId,
            CartStatus.ACTIVE,
            null,
            CartTotals.empty(resolved),
            List.of(),
            now,
            now,
            0
        );
    }

    public static Cart rehydrate(CartId cartId,
                                 String customerId,
                                 String sessionId,
                                 CartStatus status,
                                 AppliedPromoCode appliedPromoCode,
                                 CartTotals totals,
                                 List<CartItem> items,
                                 Instant createdAt,
                                 Instant updatedAt,
                                 int version) {
        return new Cart(
            cartId,
            customerId,
            sessionId,
            status,
            appliedPromoCode,
            totals,
            items,
            createdAt,
            updatedAt,
            version
        );
    }

    public CartId cartId() {
        return cartId;
    }

    public String customerId() {
        return customerId;
    }

    public String sessionId() {
        return sessionId;
    }

    public CartStatus status() {
        return status;
    }

    public AppliedPromoCode appliedPromoCode() {
        return appliedPromoCode;
    }

    public CartTotals totals() {
        return totals;
    }

    public List<CartItem> items() {
        return List.copyOf(items);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public int version() {
        return version;
    }

    public Optional<CartItem> findItem(CartItemId itemId) {
        return items.stream().filter(item -> item.itemId().equals(itemId)).findFirst();
    }

    public Optional<CartItem> findItemByHash(String configurationHash) {
        return items.stream().filter(item -> item.configurationHash().equals(configurationHash)).findFirst();
    }

    public CartItem addItem(CartItem item, CartTotalsCalculator calculator, Money taxOverride) {
        ensureModifiable();
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(calculator, "calculator cannot be null");
        if (status == CartStatus.ABANDONED) {
            status = CartStatus.ACTIVE;
        }
        CartItem merged = findItemByHash(item.configurationHash()).orElse(null);
        if (merged != null) {
            merged.increaseQuantity(item.quantity());
        } else {
            items.add(item);
            merged = item;
        }
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
        return merged;
    }

    public void updateItemQuantity(CartItemId itemId, int quantity, CartTotalsCalculator calculator, Money taxOverride) {
        ensureModifiable();
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(calculator, "calculator cannot be null");
        CartItem item = findItem(itemId).orElseThrow(() ->
            new IllegalArgumentException("Item not found: " + itemId));
        // BA-CART-UPDATE-03: Update quantity and line total
        item.updateQuantity(quantity);
        // BA-CART-UPDATE-04: Recalculate cart totals
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public void removeItem(CartItemId itemId, CartTotalsCalculator calculator, Money taxOverride) {
        ensureModifiable();
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(calculator, "calculator cannot be null");
        boolean removed = items.removeIf(item -> item.itemId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public void clear(CartTotalsCalculator calculator) {
        ensureModifiable();
        Objects.requireNonNull(calculator, "calculator cannot be null");
        // BA-CART-CLEAR-03: Remove all items from cart
        items.clear();
        // BA-CART-CLEAR-04: Remove applied promo code
        appliedPromoCode = null;
        status = CartStatus.ACTIVE;
        // BA-CART-CLEAR-05: Reset cart totals to zero
        recalculateTotals(calculator, null);
        updatedAt = Instant.now();
        version++;
    }

    public void applyPromoCode(AppliedPromoCode promo, CartTotalsCalculator calculator, Money taxOverride) {
        ensureModifiable();
        Objects.requireNonNull(promo, "promo cannot be null");
        Objects.requireNonNull(calculator, "calculator cannot be null");
        this.appliedPromoCode = promo;
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public void removePromoCode(CartTotalsCalculator calculator, Money taxOverride) {
        ensureModifiable();
        Objects.requireNonNull(calculator, "calculator cannot be null");
        this.appliedPromoCode = null;
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public void recalculatePromoDiscount(AppliedPromoCode promo, CartTotalsCalculator calculator, Money taxOverride) {
        // BA-CART-REFRESH-06: Recalculate promo discount if applied
        Objects.requireNonNull(calculator, "calculator cannot be null");
        this.appliedPromoCode = promo;
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public void calculateTotals(CartTotalsCalculator calculator, Money taxOverride) {
        // BA-CART-REFRESH-05: Recalculate cart totals
        Objects.requireNonNull(calculator, "calculator cannot be null");
        recalculateTotals(calculator, taxOverride);
        updatedAt = Instant.now();
        version++;
    }

    public CartSnapshot createSnapshot(SnapshotId snapshotId, Duration validity, Instant now) {
        // BA-CART-SNAP-04: Create immutable snapshot
        Objects.requireNonNull(snapshotId, "snapshotId cannot be null");
        Objects.requireNonNull(validity, "validity cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        List<CartSnapshotItem> snapshotItems = items.stream()
            .map(CartSnapshotItem::fromCartItem)
            .toList();
        return CartSnapshot.create(
            snapshotId,
            cartId,
            customerId,
            snapshotItems,
            totals,
            appliedPromoCode,
            now,
            now.plus(validity)
        );
    }

    public void markCheckedOut() {
        // BA-CART-SNAP-05: Clear original cart (set status CHECKED_OUT)
        status = CartStatus.CHECKED_OUT;
        updatedAt = Instant.now();
        version++;
    }

    public void markMerged() {
        status = CartStatus.MERGED;
        updatedAt = Instant.now();
        version++;
    }

    public void markAbandoned() {
        status = CartStatus.ABANDONED;
        updatedAt = Instant.now();
        version++;
    }

    private void recalculateTotals(CartTotalsCalculator calculator, Money taxOverride) {
        totals = calculator.calculateTotals(items, appliedPromoCode, taxOverride);
    }

    private void ensureModifiable() {
        if (status == CartStatus.CHECKED_OUT || status == CartStatus.MERGED) {
            throw new IllegalStateException("Cart is not modifiable in status " + status);
        }
    }
}
