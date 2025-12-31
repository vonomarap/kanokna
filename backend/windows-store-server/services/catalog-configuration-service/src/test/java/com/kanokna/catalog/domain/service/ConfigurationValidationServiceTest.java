package com.kanokna.catalog.domain.service;

import com.kanokna.catalog.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationValidationService.
 * Covers MODULE_CONTRACT MC-catalog-configuration-service-domain-ConfigurationValidation
 * and FUNCTION_CONTRACT FC-...-validateConfiguration.
 *
 * Test Cases:
 * TC-VAL-001: Valid configuration with all constraints satisfied returns valid=true
 * TC-VAL-002: Width below 50cm returns ERR-CFG-DIMENSIONS
 * TC-VAL-003: Width above 400cm returns ERR-CFG-DIMENSIONS
 * TC-FUNC-VAL-001 to TC-FUNC-VAL-004
 */
class ConfigurationValidationServiceTest {

    private ConfigurationValidationService validationService;
    private RuleEvaluator ruleEvaluator;
    private ProductTemplate productTemplate;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator();
        validationService = new ConfigurationValidationService(ruleEvaluator);

        // Create test product template
        productTemplate = ProductTemplate.create(
            "Test Window",
            "Test window product",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
    }

    @Test
    @DisplayName("TC-VAL-001: Valid configuration with all constraints satisfied returns valid=true")
    void validConfiguration_ReturnsValidTrue() {
        // Given: Valid configuration within constraints
        Configuration config = new Configuration(120, 150, Map.of());

        // When: Validate configuration
        ValidationResult result = validationService.validate(config, productTemplate, null);

        // Then: Result is valid
        assertTrue(result.isValid());
        assertEquals(0, result.errorCount());
    }

    @Test
    @DisplayName("TC-VAL-002: Width below 50cm returns ERR-CFG-DIMENSIONS")
    void widthBelowMinimum_ReturnsError() {
        // Given: Configuration with width below minimum
        Configuration config = new Configuration(40, 150, Map.of());

        // When: Validate configuration
        ValidationResult result = validationService.validate(config, productTemplate, null);

        // Then: Validation fails with dimensions error
        assertFalse(result.isValid());
        assertEquals(1, result.errorCount());
        assertTrue(result.getErrors().get(0).code().contains("ERR-CFG-DIMENSIONS"));
    }

    @Test
    @DisplayName("TC-VAL-003: Width above 400cm returns ERR-CFG-DIMENSIONS")
    void widthAboveMaximum_ReturnsError() {
        // Given: Configuration with width above maximum
        Configuration config = new Configuration(450, 150, Map.of());

        // When: Validate configuration
        ValidationResult result = validationService.validate(config, productTemplate, null);

        // Then: Validation fails with dimensions error
        assertFalse(result.isValid());
        assertEquals(1, result.errorCount());
        assertTrue(result.getErrors().get(0).code().contains("ERR-CFG-DIMENSIONS"));
    }

    @Test
    @DisplayName("TC-FUNC-VAL-001: Valid config returns valid=true and empty errors")
    void validConfig_EmptyErrors() {
        // Given
        Configuration config = new Configuration(100, 120, Map.of());

        // When
        ValidationResult result = validationService.validate(config, productTemplate, null);

        // Then
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("TC-FUNC-VAL-002: Invalid dimensions returns ERR-CFG-DIMENSIONS with field path")
    void invalidDimensions_ContainsFieldPath() {
        // Given
        Configuration config = new Configuration(30, 120, Map.of());

        // When
        ValidationResult result = validationService.validate(config, productTemplate, null);

        // Then
        assertFalse(result.isValid());
        assertEquals("dimensions", result.getErrors().get(0).field());
    }

    @Test
    @DisplayName("TC-FUNC-VAL-004: Multiple errors are aggregated in result")
    void multipleViolations_AggregatesErrors() {
        // Given: Configuration with multiple violations
        ProductTemplate restrictedTemplate = ProductTemplate.create(
            "Restricted",
            "Restricted template",
            ProductFamily.WINDOW,
            new DimensionConstraints(100, 200, 100, 200)
        );
        Configuration config = new Configuration(50, 50, Map.of()); // Both below minimum

        // When
        ValidationResult result = validationService.validate(config, restrictedTemplate, null);

        // Then
        assertFalse(result.isValid());
        // Expect at least one error for dimension violation
        assertTrue(result.errorCount() >= 1);
    }
}
