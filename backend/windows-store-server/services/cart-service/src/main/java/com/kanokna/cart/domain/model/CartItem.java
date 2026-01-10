package com.kanokna.cart.domain.model;

import com.kanokna.shared.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Cart line item with configuration snapshot and pricing reference.
 */
public class CartItem {
    private final CartItemId itemId;
    private final String productTemplateId;
    private final String productName;
    private final String productFamily;
    private final ConfigurationSnapshot configurationSnapshot;
    private final String configurationHash;
    private int quantity;
    private Money unitPrice;
    private Money lineTotal;
    private PriceQuoteReference quoteReference;
    private ValidationStatus validationStatus;
    private String validationMessage;
    private String thumbnailUrl;
    private boolean priceStale;
    private final Instant createdAt;
    private Instant updatedAt;

    private CartItem(CartItemId itemId,
                     String productTemplateId,
                     String productName,
                     String productFamily,
                     ConfigurationSnapshot configurationSnapshot,
                     String configurationHash,
                     int quantity,
                     Money unitPrice,
                     PriceQuoteReference quoteReference,
                     ValidationStatus validationStatus,
                     String validationMessage,
                     String thumbnailUrl,
                     boolean priceStale,
                     Instant createdAt,
                     Instant updatedAt) {
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.productTemplateId = Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        this.productName = Objects.requireNonNull(productName, "productName cannot be null");
        this.productFamily = productFamily;
        this.configurationSnapshot = Objects.requireNonNull(configurationSnapshot, "configurationSnapshot cannot be null");
        this.configurationHash = Objects.requireNonNull(configurationHash, "configurationHash cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        this.quoteReference = Objects.requireNonNull(quoteReference, "quoteReference cannot be null");
        this.validationStatus = Objects.requireNonNull(validationStatus, "validationStatus cannot be null");
        this.validationMessage = validationMessage;
        this.thumbnailUrl = thumbnailUrl;
        this.priceStale = priceStale;
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiplyBy(BigDecimal.valueOf(quantity));
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public static CartItem create(String productTemplateId,
                                  String productName,
                                  String productFamily,
                                  ConfigurationSnapshot configurationSnapshot,
                                  String configurationHash,
                                  int quantity,
                                  Money unitPrice,
                                  PriceQuoteReference quoteReference,
                                  ValidationStatus validationStatus,
                                  String validationMessage,
                                  String thumbnailUrl,
                                  boolean priceStale,
                                  Instant now) {
        return new CartItem(
            CartItemId.generate(),
            productTemplateId,
            productName,
            productFamily,
            configurationSnapshot,
            configurationHash,
            quantity,
            unitPrice,
            quoteReference,
            validationStatus,
            validationMessage,
            thumbnailUrl,
            priceStale,
            now,
            now
        );
    }

    public static CartItem rehydrate(CartItemId itemId,
                                     String productTemplateId,
                                     String productName,
                                     String productFamily,
                                     ConfigurationSnapshot configurationSnapshot,
                                     String configurationHash,
                                     int quantity,
                                     Money unitPrice,
                                     PriceQuoteReference quoteReference,
                                     ValidationStatus validationStatus,
                                     String validationMessage,
                                     String thumbnailUrl,
                                     boolean priceStale,
                                     Instant createdAt,
                                     Instant updatedAt) {
        return new CartItem(
            itemId,
            productTemplateId,
            productName,
            productFamily,
            configurationSnapshot,
            configurationHash,
            quantity,
            unitPrice,
            quoteReference,
            validationStatus,
            validationMessage,
            thumbnailUrl,
            priceStale,
            createdAt,
            updatedAt
        );
    }

    public CartItemId itemId() {
        return itemId;
    }

    public String productTemplateId() {
        return productTemplateId;
    }

    public String productName() {
        return productName;
    }

    public String productFamily() {
        return productFamily;
    }

    public ConfigurationSnapshot configurationSnapshot() {
        return configurationSnapshot;
    }

    public String configurationHash() {
        return configurationHash;
    }

    public int quantity() {
        return quantity;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Money lineTotal() {
        return lineTotal;
    }

    public PriceQuoteReference quoteReference() {
        return quoteReference;
    }

    public ValidationStatus validationStatus() {
        return validationStatus;
    }

    public String validationMessage() {
        return validationMessage;
    }

    public String thumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean priceStale() {
        return priceStale;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void updateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiplyBy(BigDecimal.valueOf(quantity));
        this.updatedAt = Instant.now();
    }

    public void increaseQuantity(int delta) {
        if (delta < 1) {
            throw new IllegalArgumentException("delta must be >= 1");
        }
        updateQuantity(this.quantity + delta);
    }

    public void updatePrice(Money unitPrice, PriceQuoteReference quoteReference) {
        // BA-CART-REFRESH-03: Update item prices, quote_id, quote_valid_until
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        this.quoteReference = Objects.requireNonNull(quoteReference, "quoteReference cannot be null");
        this.lineTotal = unitPrice.multiplyBy(BigDecimal.valueOf(quantity));
        this.priceStale = false;
        this.updatedAt = Instant.now();
    }

    public void updateValidationStatus(ValidationStatus status, String message) {
        this.validationStatus = Objects.requireNonNull(status, "status cannot be null");
        this.validationMessage = message;
        this.updatedAt = Instant.now();
    }

    public void markPriceStale(boolean stale) {
        this.priceStale = stale;
        this.updatedAt = Instant.now();
    }

    public void clearPriceStale() {
        // BA-CART-REFRESH-04: Clear price_stale flags
        this.priceStale = false;
        this.updatedAt = Instant.now();
    }
}
