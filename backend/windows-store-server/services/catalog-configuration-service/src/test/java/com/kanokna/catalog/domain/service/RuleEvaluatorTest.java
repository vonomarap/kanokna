package com.kanokna.catalog.domain.service;

import com.kanokna.catalog.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RuleEvaluator.
 */
class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;
    private ConfigurationRuleSet ruleSet;
    private ProductTemplateId productTemplateId;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator();
        productTemplateId = ProductTemplateId.generate();
        ruleSet = ConfigurationRuleSet.create(productTemplateId);
    }

    @Test
    @DisplayName("Empty rule set returns no errors")
    void emptyRuleSet_NoErrors() {
        // Given
        Configuration config = new Configuration(120, 150, Map.of());

        // When
        List<ValidationError> errors = ruleEvaluator.evaluateCompatibilityRules(config, ruleSet);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Dependency rule evaluates correctly")
    void dependencyRule_Evaluates() {
        // Given
        UUID sourceOption = UUID.randomUUID();
        UUID targetOption = UUID.randomUUID();

        ConfigurationRule rule = ConfigurationRule.createDependencyRule(
            sourceOption,
            targetOption,
            "ERR-CFG-DEPENDENCY",
            "Target required when source selected"
        );
        ruleSet.addRule(rule);

        Configuration configWithSource = new Configuration(120, 150,
            Map.of("group1", sourceOption));

        // When
        List<ValidationError> errors = ruleEvaluator.evaluateDependencyRules(configWithSource, ruleSet);

        // Then: Should fail because target is missing
        assertEquals(1, errors.size());
        assertEquals("ERR-CFG-DEPENDENCY", errors.get(0).code());
    }

    @Test
    @DisplayName("Exclusion rule detects conflicting options")
    void exclusionRule_DetectsConflict() {
        // Given
        UUID option1 = UUID.randomUUID();
        UUID option2 = UUID.randomUUID();

        ConfigurationRule rule = new ConfigurationRule(
            UUID.randomUUID(),
            RuleType.EXCLUSION,
            option1,
            option2,
            null,
            "ERR-CFG-EXCLUSION",
            "Options are mutually exclusive"
        );
        ruleSet.addRule(rule);

        Configuration configWithBoth = new Configuration(120, 150,
            Map.of("group1", option1, "group2", option2));

        // When
        List<ValidationError> errors = ruleEvaluator.evaluateExclusionRules(configWithBoth, ruleSet);

        // Then
        assertEquals(1, errors.size());
        assertEquals("ERR-CFG-EXCLUSION", errors.get(0).code());
    }
}
