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
    private final ConfigurationSnapshot configurationSnapshot;
    private final String configurationHash;
    private int quantity;
    private Money unitPrice;
    private Money lineTotal;
    private PriceQuoteReference quoteReference;
    private ValidationStatus validationStatus;
    private String validationMessage;
    private String thumbnailUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private CartItem(CartItemId itemId,
                     String productTemplateId,
                     String productName,
                     ConfigurationSnapshot configurationSnapshot,
                     String configurationHash,
                     int quantity,
                     Money unitPrice,
                     PriceQuoteReference quoteReference,
                     ValidationStatus validationStatus,
                     String validationMessage,
                     String thumbnailUrl,
                     Instant createdAt,
                     Instant updatedAt) {
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.productTemplateId = Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        this.productName = Objects.requireNonNull(productName, "productName cannot be null");
        this.configurationSnapshot = Objects.requireNonNull(configurationSnapshot, "configurationSnapshot cannot be null");
        this.configurationHash = Objects.requireNonNull(configurationHash, "configurationHash cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        this.quoteReference = Objects.requireNonNull(quoteReference, "quoteReference cannot be null");
        this.validationStatus = Objects.requireNonNull(validationStatus, "validationStatus cannot be null");
        this.validationMessage = validationMessage;
        this.thumbnailUrl = thumbnailUrl;
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
                                  ConfigurationSnapshot configurationSnapshot,
                                  String configurationHash,
                                  int quantity,
                                  Money unitPrice,
                                  PriceQuoteReference quoteReference,
                                  ValidationStatus validationStatus,
                                  String validationMessage,
                                  String thumbnailUrl,
                                  Instant now) {
        return new CartItem(
            CartItemId.generate(),
            productTemplateId,
            productName,
            configurationSnapshot,
            configurationHash,
            quantity,
            unitPrice,
            quoteReference,
            validationStatus,
            validationMessage,
            thumbnailUrl,
            now,
            now
        );
    }

    public static CartItem rehydrate(CartItemId itemId,
                                     String productTemplateId,
                                     String productName,
                                     ConfigurationSnapshot configurationSnapshot,
                                     String configurationHash,
                                     int quantity,
                                     Money unitPrice,
                                     PriceQuoteReference quoteReference,
                                     ValidationStatus validationStatus,
                                     String validationMessage,
                                     String thumbnailUrl,
                                     Instant createdAt,
                                     Instant updatedAt) {
        return new CartItem(
            itemId,
            productTemplateId,
            productName,
            configurationSnapshot,
            configurationHash,
            quantity,
            unitPrice,
            quoteReference,
            validationStatus,
            validationMessage,
            thumbnailUrl,
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
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        this.quoteReference = Objects.requireNonNull(quoteReference, "quoteReference cannot be null");
        this.lineTotal = unitPrice.multiplyBy(BigDecimal.valueOf(quantity));
        this.updatedAt = Instant.now();
    }

    public void updateValidationStatus(ValidationStatus status, String message) {
        this.validationStatus = Objects.requireNonNull(status, "status cannot be null");
        this.validationMessage = message;
        this.updatedAt = Instant.now();
    }
}
