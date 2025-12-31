package com.kanokna.catalog.domain.exception;

import com.kanokna.catalog.domain.model.ValidationError;
import com.kanokna.catalog.domain.model.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvalidConfigurationException.
 */
class InvalidConfigurationExceptionTest {

    @Test
    @DisplayName("Exception contains validation result")
    void exception_ContainsValidationResult() {
        // Given
        ValidationError error = ValidationError.of("ERR-001", "Error message", "field");
        ValidationResult result = ValidationResult.failure(List.of(error));

        // When
        InvalidConfigurationException exception = new InvalidConfigurationException(result);

        // Then
        assertEquals(result, exception.getValidationResult());
        assertTrue(exception.getMessage().contains("1"));
    }
}
