package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Configuration value object.
 */
class ConfigurationTest {

    @Test
    @DisplayName("Configuration with valid dimensions is created successfully")
    void validDimensions_CreatesConfiguration() {
        // Given
        int width = 120;
        int height = 150;
        Map<String, UUID> options = Map.of("material", UUID.randomUUID());

        // When
        Configuration config = new Configuration(width, height, options);

        // Then
        assertEquals(width, config.widthCm());
        assertEquals(height, config.heightCm());
        assertEquals(1, config.selectedOptions().size());
    }

    @Test
    @DisplayName("Area calculation is correct")
    void areaCalculation_IsCorrect() {
        // Given
        Configuration config = new Configuration(100, 200, Map.of());

        // When
        double area = config.getAreaM2();

        // Then
        assertEquals(2.0, area, 0.01);
    }

    @Test
    @DisplayName("Zero or negative dimensions throw exception")
    void invalidDimensions_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> new Configuration(0, 100, Map.of()));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuration(100, -50, Map.of()));
    }

    @Test
    @DisplayName("Null options map throws exception")
    void nullOptions_ThrowsException() {
        // When/Then
        assertThrows(NullPointerException.class,
            () -> new Configuration(100, 100, null));
    }
}
