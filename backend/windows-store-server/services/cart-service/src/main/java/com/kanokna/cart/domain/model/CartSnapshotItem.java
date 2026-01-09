package com.kanokna.cart.domain.model;

import com.kanokna.shared.money.Money;
import java.util.Objects;

/**
 * Immutable item snapshot for checkout.
 */
public record CartSnapshotItem(
    String itemId,
    String productTemplateId,
    String productName,
    ConfigurationSnapshot configurationSnapshot,
    String configurationHash,
    int quantity,
    Money unitPrice,
    Money lineTotal,
    PriceQuoteReference quoteReference
) {
    public CartSnapshotItem {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        Objects.requireNonNull(productName, "productName cannot be null");
        Objects.requireNonNull(configurationSnapshot, "configurationSnapshot cannot be null");
        Objects.requireNonNull(configurationHash, "configurationHash cannot be null");
        Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        Objects.requireNonNull(lineTotal, "lineTotal cannot be null");
        Objects.requireNonNull(quoteReference, "quoteReference cannot be null");
    }

    public static CartSnapshotItem fromCartItem(CartItem item) {
        return new CartSnapshotItem(
            item.itemId().toString(),
            item.productTemplateId(),
            item.productName(),
            item.configurationSnapshot(),
            item.configurationHash(),
            item.quantity(),
            item.unitPrice(),
            item.lineTotal(),
            item.quoteReference()
        );
    }
}
