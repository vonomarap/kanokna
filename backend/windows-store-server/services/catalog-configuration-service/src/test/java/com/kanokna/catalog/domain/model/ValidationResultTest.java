package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationResult value object.
 */
class ValidationResultTest {

    @Test
    @DisplayName("Success result is valid with no errors")
    void success_IsValid() {
        // When
        ValidationResult result = ValidationResult.success();

        // Then
        assertTrue(result.isValid());
        assertEquals(0, result.errorCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Failure result is invalid with errors")
    void failure_IsInvalid() {
        // Given
        ValidationError error = ValidationError.of("ERR-001", "Test error", "field");

        // When
        ValidationResult result = ValidationResult.failure(error);

        // Then
        assertFalse(result.isValid());
        assertEquals(1, result.errorCount());
        assertEquals("ERR-001", result.getErrors().get(0).code());
    }

    @Test
    @DisplayName("Failure with empty errors throws exception")
    void failureWithEmptyErrors_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> ValidationResult.failure(List.of()));
    }

    @Test
    @DisplayName("Multiple errors are preserved")
    void multipleErrors_Preserved() {
        // Given
        List<ValidationError> errors = List.of(
            ValidationError.of("ERR-001", "Error 1", "field1"),
            ValidationError.of("ERR-002", "Error 2", "field2")
        );

        // When
        ValidationResult result = ValidationResult.failure(errors);

        // Then
        assertFalse(result.isValid());
        assertEquals(2, result.errorCount());
    }
}
