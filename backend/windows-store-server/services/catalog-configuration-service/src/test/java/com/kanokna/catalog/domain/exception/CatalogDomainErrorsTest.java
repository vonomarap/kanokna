package com.kanokna.catalog.domain.exception;

import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CatalogDomainErrors factory.
 */
class CatalogDomainErrorsTest {

    private static final String DIMENSION_NAME = "widthCm";
    private static final int DIMENSION_VALUE = 10;
    private static final int MIN_VALUE = 50;
    private static final int MAX_VALUE = 400;

    private static final String WIDTH_DIMENSION = "width";
    private static final int WIDTH_MIN = 300;
    private static final int WIDTH_MAX = 200;

    private static final int INVALID_VERSION = 0;

    @Test
    @DisplayName("dimensionOutOfRange includes all context in message")
    void dimensionOutOfRange_includesAllContextInMessage() {
        DomainException ex = CatalogDomainErrors.dimensionOutOfRange(
            DIMENSION_NAME,
            DIMENSION_VALUE,
            MIN_VALUE,
            MAX_VALUE
        );

        assertEquals("ERR-CAT-DIMENSION-OUT-OF-RANGE", ex.getCode());
        assertTrue(ex.getMessage().contains(DIMENSION_NAME));
        assertTrue(ex.getMessage().contains(String.valueOf(DIMENSION_VALUE)));
        assertTrue(ex.getMessage().contains(String.valueOf(MIN_VALUE)));
        assertTrue(ex.getMessage().contains(String.valueOf(MAX_VALUE)));
    }

    @Test
    @DisplayName("minExceedsMax includes both values in message")
    void minExceedsMax_includesBothValuesInMessage() {
        DomainException ex = CatalogDomainErrors.minExceedsMax(
            WIDTH_DIMENSION,
            WIDTH_MIN,
            WIDTH_MAX
        );

        assertEquals("ERR-CAT-DIMENSION-MIN-EXCEEDS-MAX", ex.getCode());
        assertTrue(ex.getMessage().contains(String.valueOf(WIDTH_MIN)));
        assertTrue(ex.getMessage().contains(String.valueOf(WIDTH_MAX)));
    }

    @Test
    @DisplayName("invalidVersionNumber includes value in message")
    void invalidVersionNumber_includesValueInMessage() {
        DomainException ex = CatalogDomainErrors.invalidVersionNumber(INVALID_VERSION);

        assertEquals("ERR-CAT-INVALID-VERSION", ex.getCode());
        assertTrue(ex.getMessage().contains(String.valueOf(INVALID_VERSION)));
    }

    @Test
    @DisplayName("emptyValidationErrors returns expected error code")
    void emptyValidationErrors_hasCorrectCode() {
        DomainException ex = CatalogDomainErrors.emptyValidationErrors();

        assertEquals("ERR-CAT-EMPTY-VALIDATION-ERRORS", ex.getCode());
    }
}
