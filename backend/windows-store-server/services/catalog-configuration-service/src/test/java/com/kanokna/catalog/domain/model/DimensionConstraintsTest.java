package com.kanokna.catalog.domain.model;

import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DimensionConstraints value object.
 */
class DimensionConstraintsTest {

    @Test
    @DisplayName("Valid constraints are created successfully")
    void validConstraints_Created() {
        // When
        DimensionConstraints constraints = new DimensionConstraints(60, 300, 80, 250);

        // Then
        assertEquals(60, constraints.minWidthCm());
        assertEquals(300, constraints.maxWidthCm());
        assertEquals(80, constraints.minHeightCm());
        assertEquals(250, constraints.maxHeightCm());
    }

    @Test
    @DisplayName("Constraints outside absolute range throw exception")
    void outsideAbsoluteRange_ThrowsException() {
        // When/Then
        assertThrows(DomainException.class,
            () -> new DimensionConstraints(40, 300, 80, 250)); // min below 50

        assertThrows(DomainException.class,
            () -> new DimensionConstraints(60, 450, 80, 250)); // max above 400
    }

    @Test
    @DisplayName("Min greater than max throws exception")
    void minGreaterThanMax_ThrowsException() {
        // When/Then
        assertThrows(DomainException.class,
            () -> new DimensionConstraints(300, 200, 80, 250)); // width min > max
    }

    @Test
    @DisplayName("Allows method correctly validates dimensions")
    void allows_ValidatesDimensions() {
        // Given
        DimensionConstraints constraints = new DimensionConstraints(60, 300, 80, 250);

        // Then
        assertTrue(constraints.allows(100, 150));
        assertTrue(constraints.allows(60, 80)); // exactly at min
        assertTrue(constraints.allows(300, 250)); // exactly at max

        assertFalse(constraints.allows(50, 150)); // width too small
        assertFalse(constraints.allows(350, 150)); // width too large
        assertFalse(constraints.allows(100, 70)); // height too small
        assertFalse(constraints.allows(100, 300)); // height too large
    }

    @Test
    @DisplayName("Standard constraints have correct default values")
    void standardConstraints_CorrectDefaults() {
        // When
        DimensionConstraints standard = DimensionConstraints.standard();

        // Then
        assertEquals(50, standard.minWidthCm());
        assertEquals(400, standard.maxWidthCm());
        assertEquals(50, standard.minHeightCm());
        assertEquals(400, standard.maxHeightCm());
    }
}
