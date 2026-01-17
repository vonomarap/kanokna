package com.kanokna.catalog.domain.model;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing the result of configuration validation.
 * Immutable.
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<ValidationError> errors;

    private ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = errors == null ? Collections.emptyList() : List.copyOf(errors);
    }

    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        Objects.requireNonNull(errors, "errors cannot be null");
        if (errors.isEmpty()) {
            throw CatalogDomainErrors.emptyValidationErrors();
        }
        return new ValidationResult(false, errors);
    }

    public static ValidationResult failure(ValidationError error) {
        return failure(List.of(error));
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public int errorCount() {
        return errors.size();
    }
}
