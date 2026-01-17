package com.kanokna.cart.domain.exception;

import java.util.List;

/**
 * Exception thrown when a product configuration is invalid.
 */
public class InvalidConfigurationException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-INVALID-CONFIG";

    private final List<String> validationErrors;

    public InvalidConfigurationException(String message) {
        super(ERROR_CODE, message);
        this.validationErrors = List.of();
    }

    public InvalidConfigurationException(String message, List<String> validationErrors) {
        super(ERROR_CODE, message);
        this.validationErrors = validationErrors != null ? List.copyOf(validationErrors) : List.of();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
