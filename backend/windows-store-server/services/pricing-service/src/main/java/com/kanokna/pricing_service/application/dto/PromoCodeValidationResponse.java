package com.kanokna.pricing_service.application.dto;

/**
 * Response DTO for promo code validation.
 */
public class PromoCodeValidationResponse {
    private final boolean valid;
    private final String errorMessage;
    private final String discountAmount;

    public PromoCodeValidationResponse(boolean valid, String errorMessage, String discountAmount) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.discountAmount = discountAmount;
    }

    public static PromoCodeValidationResponse valid(String discountAmount) {
        return new PromoCodeValidationResponse(true, null, discountAmount);
    }

    public static PromoCodeValidationResponse invalid(String errorMessage) {
        return new PromoCodeValidationResponse(false, errorMessage, null);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }
}

