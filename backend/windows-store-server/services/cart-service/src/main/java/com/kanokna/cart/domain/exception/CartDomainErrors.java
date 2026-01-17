package com.kanokna.cart.domain.exception;

import com.kanokna.cart.domain.model.CartStatus;
import java.util.List;

/**
 * MODULE_CONTRACT id="MC-cart-domain-errors"
 * LAYER="domain.exception"
 * INTENT="Static factory for all cart domain exceptions; centralizes error creation"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE"
 *
 * Factory methods for cart domain errors.
 * All cart exceptions should be created through this class to ensure
 * consistent error codes and messages.
 */
public final class CartDomainErrors {

    private CartDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    // ========== Cart Not Found Errors ==========

    public static CartNotFoundException cartNotFound(String customerId, String sessionId) {
        return new CartNotFoundException(customerId, sessionId);
    }

    public static CartNotFoundException cartNotFoundForCustomer(String customerId) {
        return new CartNotFoundException(customerId, null);
    }

    public static CartNotFoundException cartNotFoundForSession(String sessionId) {
        return new CartNotFoundException(null, sessionId);
    }

    // ========== Cart Item Not Found Errors ==========

    public static CartItemNotFoundException itemNotFound(String itemId) {
        return new CartItemNotFoundException(itemId);
    }

    // ========== Invalid Quantity Errors ==========

    public static InvalidQuantityException invalidQuantity(int quantity) {
        return new InvalidQuantityException(quantity);
    }

    public static InvalidQuantityException quantityOutOfRange(int quantity, int min, int max) {
        return new InvalidQuantityException(quantity, min, max);
    }

    // ========== Invalid Configuration Errors ==========

    public static InvalidConfigurationException invalidConfiguration(String message) {
        return new InvalidConfigurationException(message);
    }

    public static InvalidConfigurationException invalidConfigurationWithErrors(String message, List<String> errors) {
        return new InvalidConfigurationException(message, errors);
    }

    public static InvalidConfigurationException missingProductTemplateId() {
        return new InvalidConfigurationException("productTemplateId is required");
    }

    public static InvalidConfigurationException missingDimensions() {
        return new InvalidConfigurationException("Dimensions (width and height) are required");
    }

    public static InvalidConfigurationException missingProductName() {
        return new InvalidConfigurationException("productName is required");
    }

    public static InvalidConfigurationException unsupportedProductFamily(String family) {
        return new InvalidConfigurationException("Unsupported product family: " + family);
    }

    // ========== Promo Code Errors ==========

    public static PromoCodeException promoInvalid(String promoCode) {
        return PromoCodeException.invalid(promoCode);
    }

    public static PromoCodeException promoMinimumNotMet(String promoCode) {
        return PromoCodeException.minimumNotMet(promoCode);
    }

    public static PromoCodeException noPromoApplied() {
        return PromoCodeException.notApplied();
    }

    // ========== Cart Merge Errors ==========

    public static CartMergeException anonymousCartNotFound(String sessionId) {
        return CartMergeException.anonymousCartNotFound(sessionId);
    }

    public static CartMergeException cannotMergeSameCart(String cartId) {
        return CartMergeException.sameCartMerge(cartId);
    }

    public static CartMergeException mergeFailed(String message) {
        return new CartMergeException(message);
    }

    public static CartMergeException mergeFailed(String message, Throwable cause) {
        return new CartMergeException(message, cause);
    }

    // ========== Checkout Errors ==========

    public static CheckoutException emptyCart() {
        return CheckoutException.emptyCart();
    }

    public static CheckoutException invalidItems(int invalidCount) {
        return CheckoutException.invalidItems(invalidCount);
    }

    public static CheckoutException anonymousNotAllowed() {
        return CheckoutException.anonymousNotAllowed();
    }

    public static CheckoutException priceChangeRequiresAcknowledgment(double changePercent) {
        return CheckoutException.priceChangeRequiresAcknowledgment(changePercent);
    }

    public static CheckoutException snapshotExpired(String snapshotId) {
        return CheckoutException.snapshotExpired(snapshotId);
    }

    // ========== Cart State Errors ==========

    public static CartStateException cartNotModifiable(CartStatus status, String operation) {
        return new CartStateException(status, operation);
    }

    // ========== External Service Errors ==========

    public static CartDomainException catalogUnavailable(String message, Throwable cause) {
        return new CartDomainException("ERR-CART-CATALOG-UNAVAILABLE", message, cause);
    }

    public static CartDomainException catalogUnavailable(String message) {
        return new CartDomainException("ERR-CART-CATALOG-UNAVAILABLE", message);
    }

    public static CartDomainException pricingUnavailable(String message, Throwable cause) {
        return new CartDomainException("ERR-CART-PRICING-UNAVAILABLE", message, cause);
    }

    public static CartDomainException pricingUnavailable(String message) {
        return new CartDomainException("ERR-CART-PRICING-UNAVAILABLE", message);
    }

    public static CartDomainException pricingPartial(String message) {
        return new CartDomainException("ERR-CART-PRICING-PARTIAL", message);
    }

    public static CartDomainException quoteExpired(String quoteId) {
        return new CartDomainException("ERR-CART-QUOTE-EXPIRED", "Quote has expired: " + quoteId);
    }

    // ========== Authorization Errors ==========

    public static CartDomainException unauthorized(String message) {
        return new CartDomainException("ERR-CART-UNAUTHORIZED", message);
    }
}
