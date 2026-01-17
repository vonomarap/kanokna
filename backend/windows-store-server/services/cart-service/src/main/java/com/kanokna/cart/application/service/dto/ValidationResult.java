package com.kanokna.cart.application.service.dto;

import com.kanokna.cart.domain.model.ValidationStatus;
import java.util.List;

/**
 * Result of cart item validation operation.
 */
public record ValidationResult(
    boolean available,
    boolean valid,
    ValidationStatus status,
    String message,
    List<String> errors
) {
    public static ValidationResult unavailable() {
        return new ValidationResult(false, false, ValidationStatus.UNKNOWN, "Catalog validation unavailable", List.of());
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, true, ValidationStatus.VALID, null, List.of());
    }

    public static ValidationResult invalid(List<String> errors) {
        String message = errors != null && !errors.isEmpty() ? String.join("; ", errors) : "Configuration invalid";
        return new ValidationResult(true, false, ValidationStatus.INVALID, message, errors != null ? errors : List.of());
    }
}
