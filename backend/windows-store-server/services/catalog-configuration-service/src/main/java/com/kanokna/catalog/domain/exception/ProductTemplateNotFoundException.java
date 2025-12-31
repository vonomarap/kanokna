package com.kanokna.catalog.domain.exception;

import com.kanokna.catalog.domain.model.ProductTemplateId;

/**
 * Exception thrown when a product template is not found.
 */
public class ProductTemplateNotFoundException extends RuntimeException {

    private final ProductTemplateId productTemplateId;

    public ProductTemplateNotFoundException(ProductTemplateId productTemplateId) {
        super("Product template not found: " + productTemplateId);
        this.productTemplateId = productTemplateId;
    }

    public ProductTemplateId getProductTemplateId() {
        return productTemplateId;
    }
}
