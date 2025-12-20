package com.kanokna.catalog_configuration_service.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ConfigurationRuleSet {
    private final List<ConfigurationRule> rules;

    public ConfigurationRuleSet(List<ConfigurationRule> rules) {
        Objects.requireNonNull(rules, "rules");
        this.rules = Collections.unmodifiableList(List.copyOf(rules));
    }

    public List<ConfigurationRule> rules() {
        return rules;
    }

    public List<RuleEvaluation> evaluate(ConfigurationSelection selection) {
        return rules.stream()
            .map(rule -> rule.evaluate(selection))
            .flatMap(Optional::stream)
            .toList();
    }

    public void assertTargets(ProductTemplate template) {
        rules.forEach(rule -> {
            Objects.requireNonNull(rule.code(), "rule code cannot be null");
            rule.requiresAll().keySet().forEach(attributeCode -> {
                if (!template.hasAttribute(attributeCode)) {
                    throw new IllegalArgumentException("Rule " + rule.code() + " references missing attribute in requiresAll: " + attributeCode);
                }
            });
            rule.prohibitsAny().keySet().forEach(attributeCode -> {
                if (!template.hasAttribute(attributeCode)) {
                    throw new IllegalArgumentException("Rule " + rule.code() + " references missing attribute in prohibitsAny: " + attributeCode);
                }
            });
            String attributeCode = rule.attributeCode();
            if (attributeCode != null && !attributeCode.isBlank() && !template.hasAttribute(attributeCode)) {
                throw new IllegalArgumentException("Rule " + rule.code() + " references unknown attribute: " + attributeCode);
            }
        });
    }
}
