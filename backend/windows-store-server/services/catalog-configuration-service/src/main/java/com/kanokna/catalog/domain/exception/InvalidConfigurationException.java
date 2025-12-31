package com.kanokna.catalog.domain.exception;

import com.kanokna.catalog.domain.model.ValidationResult;

/**
 * Exception thrown when a configuration fails validation.
 */
public class InvalidConfigurationException extends RuntimeException {

    private final ValidationResult validationResult;

    public InvalidConfigurationException(ValidationResult validationResult) {
        super("Configuration validation failed with " + validationResult.errorCount() + " error(s)");
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
