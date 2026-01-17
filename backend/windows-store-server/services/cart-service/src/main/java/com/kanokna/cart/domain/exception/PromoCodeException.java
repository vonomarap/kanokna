package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when promo code operations fail.
 */
public class PromoCodeException extends CartDomainException {

    private static final long serialVersionUID = 1L;

    private final String promoCode;

    public PromoCodeException(String errorCode, String promoCode, String message) {
        super(errorCode, message);
        this.promoCode = promoCode;
    }

    public static PromoCodeException invalid(String promoCode) {
        return new PromoCodeException(
            "ERR-CART-PROMO-INVALID",
            promoCode,
            "Promo code invalid or expired: " + promoCode
        );
    }

    public static PromoCodeException minimumNotMet(String promoCode) {
        return new PromoCodeException(
            "ERR-CART-PROMO-MIN-NOT-MET",
            promoCode,
            "Promo code minimum order amount not met: " + promoCode
        );
    }

    public static PromoCodeException notApplied() {
        return new PromoCodeException(
            "ERR-CART-NO-PROMO",
            null,
            "No promo code is currently applied"
        );
    }

    public String getPromoCode() {
        return promoCode;
    }
}
