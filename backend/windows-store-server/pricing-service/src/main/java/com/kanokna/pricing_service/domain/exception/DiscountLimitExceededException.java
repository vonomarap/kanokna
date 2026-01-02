package com.kanokna.pricing_service.domain.exception;

/**
 * Exception thrown when combined discounts exceed the 30% cap.
 * Error code: ERR-PRC-DISCOUNT-EXCEEDED
 */
public class DiscountLimitExceededException extends RuntimeException {
    private final String totalDiscountPercent;

    public DiscountLimitExceededException(String totalDiscountPercent) {
        super(String.format("Combined discounts (%s%%) exceed the 30%% cap", totalDiscountPercent));
        this.totalDiscountPercent = totalDiscountPercent;
    }

    public String getTotalDiscountPercent() {
        return totalDiscountPercent;
    }

    public String getErrorCode() {
        return "ERR-PRC-DISCOUNT-EXCEEDED";
    }
}

