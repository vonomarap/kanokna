package com.kanokna.catalog.domain.exception;

import com.kanokna.shared.core.DomainException;

/**
 * MODULE_CONTRACT id="MC-catalog-domain-errors"
 * LAYER="domain.exception"
 * INTENT="Static factory for catalog-configuration-service domain exceptions"
 * LINKS="Technology.xml#DEC-DOMAIN-ERROR-PATTERN;RequirementsAnalysis.xml#NFR-CODE-DOMAIN-ERROR-HANDLING"
 *
 * Factory methods for catalog domain error codes.
 * All catalog exceptions should be created through this class to ensure
 * consistent error codes and messages.
 *
 * @see DomainException
 */
public final class CatalogDomainErrors {

    private CatalogDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    // ========== Dimension Validation Errors ==========

    /* <FUNCTION_CONTRACT id="FC-catalog-CatalogDomainErrors-dimensionOutOfRange"
         LAYER="domain.exception"
         INTENT="Create exception for dimension constraint violations"
         INPUT="dimension name (String), value (int), min (int), max (int)"
         OUTPUT="DomainException with ERR-CAT-DIMENSION-OUT-OF-RANGE code"
         ERROR_CODE="ERR-CAT-DIMENSION-OUT-OF-RANGE"
         LINKS="MC-catalog-domain-errors">
    </FUNCTION_CONTRACT> */
    public static DomainException dimensionOutOfRange(String dimension, int value, int min, int max) {
        return new DomainException("ERR-CAT-DIMENSION-OUT-OF-RANGE",
            String.format("%s=%d is outside valid range [%d, %d]", dimension, value, min, max));
    }

    public static DomainException minExceedsMax(String dimension, int min, int max) {
        return new DomainException("ERR-CAT-DIMENSION-MIN-EXCEEDS-MAX",
            String.format("min%sCm=%d cannot exceed max%sCm=%d",
                capitalize(dimension), min, capitalize(dimension), max));
    }

    public static DomainException invalidWidth(int width) {
        return new DomainException("ERR-CAT-INVALID-WIDTH",
            "widthCm must be positive, got: " + width);
    }

    public static DomainException invalidHeight(int height) {
        return new DomainException("ERR-CAT-INVALID-HEIGHT",
            "heightCm must be positive, got: " + height);
    }

    // ========== Version/Count Validation Errors ==========

    public static DomainException invalidVersionNumber(int version) {
        return new DomainException("ERR-CAT-INVALID-VERSION",
            "versionNumber must be positive, got: " + version);
    }

    public static DomainException invalidTemplateCount(int count) {
        return new DomainException("ERR-CAT-INVALID-TEMPLATE-COUNT",
            "templateCount cannot be negative, got: " + count);
    }

    public static DomainException invalidErrorCount(int count) {
        return new DomainException("ERR-CAT-INVALID-ERROR-COUNT",
            "errorCount cannot be negative, got: " + count);
    }

    public static DomainException invalidQuantity(int quantity) {
        return new DomainException("ERR-CAT-INVALID-QUANTITY",
            "quantity must be positive, got: " + quantity);
    }

    // ========== Validation Result Errors ==========

    public static DomainException emptyValidationErrors() {
        return new DomainException("ERR-CAT-EMPTY-VALIDATION-ERRORS",
            "Cannot create failure ValidationResult with empty errors list");
    }

    // ========== Helper ==========

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
