package com.kanokna.catalog.domain.model;

import java.util.Objects;

/**
 * Value object representing a single validation error.
 */
public record ValidationError(
    String code,
    String message,
    String field
) {

    public ValidationError {
        Objects.requireNonNull(code, "error code cannot be null");
        Objects.requireNonNull(message, "error message cannot be null");
    }

    public static ValidationError of(String code, String message, String field) {
        return new ValidationError(code, message, field);
    }

    public static ValidationError of(String code, String message) {
        return new ValidationError(code, message, null);
    }
}
