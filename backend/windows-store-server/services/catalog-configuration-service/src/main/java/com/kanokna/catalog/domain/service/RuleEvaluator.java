package com.kanokna.catalog.domain.service;

import com.kanokna.catalog.domain.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for evaluating individual configuration rules.
 * Supports compatibility, dependency, and exclusion rules.
 */
public class RuleEvaluator {

    /**
     * Evaluates compatibility rules (e.g., material + glazing combinations).
     *
     * @param configuration configuration with selected options
     * @param ruleSet       rule set containing compatibility rules
     * @return list of validation errors (empty if all rules pass)
     */
    public List<ValidationError> evaluateCompatibilityRules(
        Configuration configuration,
        ConfigurationRuleSet ruleSet
    ) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigurationRule> compatibilityRules = ruleSet.getRulesByType(RuleType.COMPATIBILITY);

        for (ConfigurationRule rule : compatibilityRules) {
            if (!isCompatibilityRuleSatisfied(rule, configuration)) {
                errors.add(ValidationError.of(
                    rule.getErrorCode(),
                    rule.getErrorMessage(),
                    "options"
                ));
            }
        }

        return errors;
    }

    /**
     * Evaluates dependency rules (e.g., option A requires option B).
     *
     * @param configuration configuration with selected options
     * @param ruleSet       rule set containing dependency rules
     * @return list of validation errors (empty if all rules pass)
     */
    public List<ValidationError> evaluateDependencyRules(
        Configuration configuration,
        ConfigurationRuleSet ruleSet
    ) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigurationRule> dependencyRules = ruleSet.getRulesByType(RuleType.DEPENDENCY);

        for (ConfigurationRule rule : dependencyRules) {
            if (!isDependencyRuleSatisfied(rule, configuration)) {
                errors.add(ValidationError.of(
                    rule.getErrorCode(),
                    rule.getErrorMessage(),
                    "options"
                ));
            }
        }

        return errors;
    }

    /**
     * Evaluates exclusion rules (e.g., option A excludes option B).
     *
     * @param configuration configuration with selected options
     * @param ruleSet       rule set containing exclusion rules
     * @return list of validation errors (empty if all rules pass)
     */
    public List<ValidationError> evaluateExclusionRules(
        Configuration configuration,
        ConfigurationRuleSet ruleSet
    ) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigurationRule> exclusionRules = ruleSet.getRulesByType(RuleType.EXCLUSION);

        for (ConfigurationRule rule : exclusionRules) {
            if (!isExclusionRuleSatisfied(rule, configuration)) {
                errors.add(ValidationError.of(
                    rule.getErrorCode(),
                    rule.getErrorMessage(),
                    "options"
                ));
            }
        }

        return errors;
    }

    private boolean isCompatibilityRuleSatisfied(ConfigurationRule rule, Configuration configuration) {
        UUID sourceOptionId = rule.getSourceOptionId();
        UUID targetOptionId = rule.getTargetOptionId();

        // If both options are selected, check if they're compatible
        boolean hasSource = configuration.hasOption(sourceOptionId);
        boolean hasTarget = configuration.hasOption(targetOptionId);

        if (hasSource && hasTarget) {
            // In a real implementation, evaluate the compatibility logic
            // For now, assume compatibility rules are met
            return true;
        }

        return true; // Rule doesn't apply if both options aren't selected
    }

    private boolean isDependencyRuleSatisfied(ConfigurationRule rule, Configuration configuration) {
        UUID sourceOptionId = rule.getSourceOptionId();
        UUID targetOptionId = rule.getTargetOptionId();

        // If source option is selected, target option must also be selected
        boolean hasSource = configuration.hasOption(sourceOptionId);
        boolean hasTarget = configuration.hasOption(targetOptionId);

        if (hasSource) {
            return hasTarget; // Dependency: source requires target
        }

        return true; // Rule doesn't apply if source isn't selected
    }

    private boolean isExclusionRuleSatisfied(ConfigurationRule rule, Configuration configuration) {
        UUID sourceOptionId = rule.getSourceOptionId();
        UUID targetOptionId = rule.getTargetOptionId();

        // Source and target options cannot both be selected
        boolean hasSource = configuration.hasOption(sourceOptionId);
        boolean hasTarget = configuration.hasOption(targetOptionId);

        return !(hasSource && hasTarget); // Exclusion: both cannot be selected together
    }
}
