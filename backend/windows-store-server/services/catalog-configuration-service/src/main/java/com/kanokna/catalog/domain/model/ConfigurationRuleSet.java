package com.kanokna.catalog.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: Set of configuration rules for a product template.
 * Versioned to support rule evolution.
 */
public class ConfigurationRuleSet {

    private final UUID id;
    private final ProductTemplateId productTemplateId;
    private int version;
    private boolean active;
    private final List<ConfigurationRule> rules;

    public ConfigurationRuleSet(UUID id, ProductTemplateId productTemplateId, int version) {
        this.id = Objects.requireNonNull(id, "RuleSet id cannot be null");
        this.productTemplateId = Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        this.version = version;
        this.active = true;
        this.rules = new ArrayList<>();
    }

    public static ConfigurationRuleSet create(ProductTemplateId productTemplateId) {
        return new ConfigurationRuleSet(UUID.randomUUID(), productTemplateId, 1);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public ProductTemplateId getProductTemplateId() {
        return productTemplateId;
    }

    public int getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }

    public List<ConfigurationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    // Business methods
    public void addRule(ConfigurationRule rule) {
        Objects.requireNonNull(rule, "Rule cannot be null");
        if (!rules.contains(rule)) {
            rules.add(rule);
        }
    }

    public void removeRule(UUID ruleId) {
        rules.removeIf(r -> r.getId().equals(ruleId));
    }

    public void incrementVersion() {
        this.version++;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public List<ConfigurationRule> getActiveRules() {
        return rules.stream()
            .filter(ConfigurationRule::isActive)
            .toList();
    }

    public List<ConfigurationRule> getRulesByType(RuleType ruleType) {
        return rules.stream()
            .filter(r -> r.getRuleType() == ruleType && r.isActive())
            .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationRuleSet that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
