package com.kanokna.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Single configuration rule within a ConfigurationRuleSet.
 * Defines compatibility, dependency, or exclusion constraints.
 */
public class ConfigurationRule {

    private final UUID id;
    private final RuleType ruleType;
    private final UUID sourceOptionId;
    private final UUID targetOptionId;
    private final String conditionExpression;
    private final String errorCode;
    private final String errorMessage;
    private boolean active;

    public ConfigurationRule(
        UUID id,
        RuleType ruleType,
        UUID sourceOptionId,
        UUID targetOptionId,
        String conditionExpression,
        String errorCode,
        String errorMessage
    ) {
        this.id = Objects.requireNonNull(id, "Rule id cannot be null");
        this.ruleType = Objects.requireNonNull(ruleType, "ruleType cannot be null");
        this.sourceOptionId = sourceOptionId;
        this.targetOptionId = targetOptionId;
        this.conditionExpression = conditionExpression;
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        this.active = true;
    }

    public static ConfigurationRule createCompatibilityRule(
        UUID sourceOptionId,
        UUID targetOptionId,
        String errorCode,
        String errorMessage
    ) {
        return new ConfigurationRule(
            UUID.randomUUID(),
            RuleType.COMPATIBILITY,
            sourceOptionId,
            targetOptionId,
            null,
            errorCode,
            errorMessage
        );
    }

    public static ConfigurationRule createDependencyRule(
        UUID sourceOptionId,
        UUID targetOptionId,
        String errorCode,
        String errorMessage
    ) {
        return new ConfigurationRule(
            UUID.randomUUID(),
            RuleType.DEPENDENCY,
            sourceOptionId,
            targetOptionId,
            null,
            errorCode,
            errorMessage
        );
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public UUID getSourceOptionId() {
        return sourceOptionId;
    }

    public UUID getTargetOptionId() {
        return targetOptionId;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isActive() {
        return active;
    }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean appliesToOptions(UUID optionId1, UUID optionId2) {
        return (sourceOptionId != null && sourceOptionId.equals(optionId1) &&
                targetOptionId != null && targetOptionId.equals(optionId2)) ||
               (sourceOptionId != null && sourceOptionId.equals(optionId2) &&
                targetOptionId != null && targetOptionId.equals(optionId1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationRule that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
