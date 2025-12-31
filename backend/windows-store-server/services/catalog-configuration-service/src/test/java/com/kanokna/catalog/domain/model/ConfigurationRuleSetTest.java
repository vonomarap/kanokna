package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationRuleSet aggregate.
 */
class ConfigurationRuleSetTest {

    @Test
    @DisplayName("Create rule set with product template ID")
    void createRuleSet_Succeeds() {
        // Given
        ProductTemplateId productTemplateId = ProductTemplateId.generate();

        // When
        ConfigurationRuleSet ruleSet = ConfigurationRuleSet.create(productTemplateId);

        // Then
        assertNotNull(ruleSet.getId());
        assertEquals(productTemplateId, ruleSet.getProductTemplateId());
        assertEquals(1, ruleSet.getVersion());
        assertTrue(ruleSet.isActive());
        assertTrue(ruleSet.getRules().isEmpty());
    }

    @Test
    @DisplayName("Add rule to rule set")
    void addRule_Succeeds() {
        // Given
        ConfigurationRuleSet ruleSet = ConfigurationRuleSet.create(ProductTemplateId.generate());
        ConfigurationRule rule = ConfigurationRule.createCompatibilityRule(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ERR-001",
            "Error message"
        );

        // When
        ruleSet.addRule(rule);

        // Then
        assertEquals(1, ruleSet.getRules().size());
    }

    @Test
    @DisplayName("Get rules by type filters correctly")
    void getRulesByType_FiltersCorrectly() {
        // Given
        ConfigurationRuleSet ruleSet = ConfigurationRuleSet.create(ProductTemplateId.generate());
        ConfigurationRule compatRule = ConfigurationRule.createCompatibilityRule(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ERR-001",
            "Compatibility error"
        );
        ConfigurationRule depRule = ConfigurationRule.createDependencyRule(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ERR-002",
            "Dependency error"
        );
        ruleSet.addRule(compatRule);
        ruleSet.addRule(depRule);

        // When
        List<ConfigurationRule> compatRules = ruleSet.getRulesByType(RuleType.COMPATIBILITY);
        List<ConfigurationRule> depRules = ruleSet.getRulesByType(RuleType.DEPENDENCY);

        // Then
        assertEquals(1, compatRules.size());
        assertEquals(1, depRules.size());
    }

    @Test
    @DisplayName("Increment version increases version number")
    void incrementVersion_IncreasesVersion() {
        // Given
        ConfigurationRuleSet ruleSet = ConfigurationRuleSet.create(ProductTemplateId.generate());
        int originalVersion = ruleSet.getVersion();

        // When
        ruleSet.incrementVersion();

        // Then
        assertEquals(originalVersion + 1, ruleSet.getVersion());
    }
}
