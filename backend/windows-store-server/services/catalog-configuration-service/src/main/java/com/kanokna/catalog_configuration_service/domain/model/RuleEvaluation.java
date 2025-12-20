package com.kanokna.catalog_configuration_service.domain.model;

import java.util.Objects;

public record RuleEvaluation(
    String ruleCode,
    RuleEffect effect,
    String message,
    String attributeCode
) {
    public RuleEvaluation {
        Objects.requireNonNull(ruleCode, "ruleCode");
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(message, "message");
    }

    public boolean isBlocking() {
        return effect == RuleEffect.DENY;
    }

    public boolean isWarning() {
        return effect == RuleEffect.WARN;
    }
}
