package com.kanokna.catalog.domain.exception;

import com.kanokna.catalog.domain.model.ProductTemplateId;

/**
 * Exception thrown when a rule set is not found for a product.
 */
public class RuleSetNotFoundException extends RuntimeException {

    private final ProductTemplateId productTemplateId;

    public RuleSetNotFoundException(ProductTemplateId productTemplateId) {
        super("Rule set not found for product template: " + productTemplateId);
        this.productTemplateId = productTemplateId;
    }

    public ProductTemplateId getProductTemplateId() {
        return productTemplateId;
    }
}
