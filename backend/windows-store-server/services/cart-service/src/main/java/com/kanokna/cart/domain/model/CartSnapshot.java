package com.kanokna.cart.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Immutable cart snapshot for checkout.
 */
public final class CartSnapshot {
    private final SnapshotId snapshotId;
    private final CartId cartId;
    private final String customerId;
    private final List<CartSnapshotItem> items;
    private final CartTotals totals;
    private final AppliedPromoCode appliedPromoCode;
    private final Instant createdAt;
    private final Instant validUntil;

    private CartSnapshot(SnapshotId snapshotId,
                         CartId cartId,
                         String customerId,
                         List<CartSnapshotItem> items,
                         CartTotals totals,
                         AppliedPromoCode appliedPromoCode,
                         Instant createdAt,
                         Instant validUntil) {
        this.snapshotId = Objects.requireNonNull(snapshotId, "snapshotId cannot be null");
        this.cartId = Objects.requireNonNull(cartId, "cartId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId cannot be null");
        this.items = List.copyOf(Objects.requireNonNull(items, "items cannot be null"));
        this.totals = Objects.requireNonNull(totals, "totals cannot be null");
        this.appliedPromoCode = appliedPromoCode;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.validUntil = Objects.requireNonNull(validUntil, "validUntil cannot be null");
    }

    public static CartSnapshot create(SnapshotId snapshotId,
                                      CartId cartId,
                                      String customerId,
                                      List<CartSnapshotItem> items,
                                      CartTotals totals,
                                      AppliedPromoCode appliedPromoCode,
                                      Instant createdAt,
                                      Instant validUntil) {
        return new CartSnapshot(
            snapshotId,
            cartId,
            customerId,
            items,
            totals,
            appliedPromoCode,
            createdAt,
            validUntil
        );
    }

    public static CartSnapshot rehydrate(SnapshotId snapshotId,
                                         CartId cartId,
                                         String customerId,
                                         List<CartSnapshotItem> items,
                                         CartTotals totals,
                                         AppliedPromoCode appliedPromoCode,
                                         Instant createdAt,
                                         Instant validUntil) {
        return new CartSnapshot(
            snapshotId,
            cartId,
            customerId,
            items,
            totals,
            appliedPromoCode,
            createdAt,
            validUntil
        );
    }

    public SnapshotId snapshotId() {
        return snapshotId;
    }

    public CartId cartId() {
        return cartId;
    }

    public String customerId() {
        return customerId;
    }

    public List<CartSnapshotItem> items() {
        return items;
    }

    public CartTotals totals() {
        return totals;
    }

    public AppliedPromoCode appliedPromoCode() {
        return appliedPromoCode;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant validUntil() {
        return validUntil;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(validUntil);
    }
}
