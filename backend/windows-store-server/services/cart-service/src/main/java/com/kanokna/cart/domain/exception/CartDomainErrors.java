package com.kanokna.cart.domain.exception;

import com.kanokna.shared.core.DomainException;

/**
 * Factory methods for cart domain error codes.
 */
public final class CartDomainErrors {
    private CartDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainException unauthorized(String message) {
        return new DomainException("ERR-CART-UNAUTHORIZED", message);
    }

    public static DomainException cartNotFound(String message) {
        return new DomainException("ERR-CART-NOT-FOUND", message);
    }

    public static DomainException catalogUnavailable(String message, Throwable cause) {
        return new DomainException("ERR-CART-CATALOG-UNAVAILABLE", message, cause);
    }

    public static DomainException pricingUnavailable(String message, Throwable cause) {
        return new DomainException("ERR-CART-PRICING-UNAVAILABLE", message, cause);
    }

    public static DomainException pricingPartial(String message) {
        return new DomainException("ERR-CART-PRICING-PARTIAL", message);
    }

    public static DomainException invalidConfiguration(String message) {
        return new DomainException("ERR-CART-INVALID-CONFIG", message);
    }

    public static DomainException quoteExpired(String quoteId) {
        return new DomainException("ERR-CART-QUOTE-EXPIRED",
            "Quote has expired: " + quoteId);
    }

    public static DomainException invalidQuantity(int quantity) {
        return new DomainException("ERR-CART-INVALID-QUANTITY",
            "Quantity must be >= 1: " + quantity);
    }

    public static DomainException itemNotFound(String itemId) {
        return new DomainException("ERR-CART-ITEM-NOT-FOUND",
            "Item not found: " + itemId);
    }

    public static DomainException promoInvalid(String promoCode) {
        return new DomainException("ERR-CART-PROMO-INVALID",
            "Promo code invalid or expired: " + promoCode);
    }

    public static DomainException promoMinimumNotMet(String promoCode) {
        return new DomainException("ERR-CART-PROMO-MIN-NOT-MET",
            "Promo code minimum not met: " + promoCode);
    }

    public static DomainException emptyCart() {
        return new DomainException("ERR-CART-EMPTY", "Cart is empty");
    }

    public static DomainException noPromoApplied() {
        return new DomainException("ERR-CART-NO-PROMO", "No promo code applied");
    }

    public static DomainException anonymousCartNotFound(String sessionId) {
        return new DomainException("ERR-CART-ANONYMOUS-NOT-FOUND",
            "Anonymous cart not found for session: " + sessionId);
    }

    public static DomainException invalidItems(String message) {
        return new DomainException("ERR-CART-INVALID-ITEMS", message);
    }

    public static DomainException anonymousNotAllowed() {
        return new DomainException("ERR-CART-ANONYMOUS",
            "Anonymous users must authenticate before checkout");
    }
}
