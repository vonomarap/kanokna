package com.kanokna.cart.domain.service;

import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import java.util.Objects;

/**
 * Domain service for merging carts.
 */
public class CartMergeService {
    public MergeResult merge(Cart source, Cart target, CartTotalsCalculator calculator) {
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(calculator, "calculator cannot be null");
        if (source.cartId().equals(target.cartId())) {
            throw new IllegalArgumentException("Cannot merge cart into itself");
        }

        int itemsFromAnonymous = source.items().size();
        int itemsMerged = 0;
        int itemsAdded = 0;

        for (CartItem item : source.items()) {
            CartItem existing = target.findItemByHash(item.configurationHash()).orElse(null);
            if (existing != null) {
                // BA-CART-MERGE-SUM: Sum quantities for matching items
                existing.increaseQuantity(item.quantity());
                itemsMerged++;
            } else {
                // BA-CART-MERGE-ADD: Add non-matching items as new
                target.addItem(cloneItem(item), calculator, null);
                itemsAdded++;
            }
        }

        AppliedPromoCode preserved = resolvePromoCode(source, target, calculator);
        String promoSource = preserved == null
            ? "NONE"
            : preserved.equals(target.appliedPromoCode()) ? "AUTHENTICATED" : "ANONYMOUS";
        boolean promoPreserved = preserved != null;

        return new MergeResult(itemsFromAnonymous, itemsMerged, itemsAdded, promoPreserved, promoSource);
    }

    private AppliedPromoCode resolvePromoCode(Cart source, Cart target, CartTotalsCalculator calculator) {
        if (target.appliedPromoCode() != null) {
            return target.appliedPromoCode();
        }
        if (source.appliedPromoCode() != null) {
            target.applyPromoCode(source.appliedPromoCode(), calculator, null);
            return source.appliedPromoCode();
        }
        return null;
    }

    private CartItem cloneItem(CartItem item) {
        return CartItem.rehydrate(
            item.itemId(),
            item.productTemplateId(),
            item.productName(),
            item.configurationSnapshot(),
            item.configurationHash(),
            item.quantity(),
            item.unitPrice(),
            item.quoteReference(),
            item.validationStatus(),
            item.validationMessage(),
            item.thumbnailUrl(),
            item.createdAt(),
            item.updatedAt()
        );
    }

    public record MergeResult(
        int itemsFromAnonymous,
        int itemsMerged,
        int itemsAdded,
        boolean promoCodePreserved,
        String promoCodeSource
    ) {
    }
}
