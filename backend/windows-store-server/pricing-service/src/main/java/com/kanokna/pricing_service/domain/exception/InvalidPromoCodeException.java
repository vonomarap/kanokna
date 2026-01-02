package com.kanokna.pricing_service.domain.exception;

/**
 * Exception thrown when a promo code is invalid, expired, or exhausted.
 * Error code: ERR-PRC-INVALID-PROMO
 */
public class InvalidPromoCodeException extends RuntimeException {
    private final String promoCode;
    private final String reason;

    public InvalidPromoCodeException(String promoCode, String reason) {
        super(String.format("Invalid promo code '%s': %s", promoCode, reason));
        this.promoCode = promoCode;
        this.reason = reason;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public String getReason() {
        return reason;
    }

    public String getErrorCode() {
        return "ERR-PRC-INVALID-PROMO";
    }
}

