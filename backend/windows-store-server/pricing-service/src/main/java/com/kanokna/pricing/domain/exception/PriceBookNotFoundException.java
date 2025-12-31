package com.kanokna.pricing.domain.exception;

/**
 * Exception thrown when no active price book is found for a product template.
 * Error code: ERR-PRC-NO-PRICEBOOK
 */
public class PriceBookNotFoundException extends RuntimeException {
    private final String productTemplateId;

    public PriceBookNotFoundException(String productTemplateId) {
        super(String.format("No active price book found for product template: %s", productTemplateId));
        this.productTemplateId = productTemplateId;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public String getErrorCode() {
        return "ERR-PRC-NO-PRICEBOOK";
    }
}
