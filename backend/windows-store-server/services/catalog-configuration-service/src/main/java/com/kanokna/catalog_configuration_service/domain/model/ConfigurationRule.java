package com.kanokna.catalog_configuration_service.domain.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ConfigurationRule {
    private final String code;
    private final RuleEffect effect;
    private final Map<String, String> requiresAll;
    private final Map<String, String> prohibitsAny;
    private final String message;
    private final String attributeCode;

    public ConfigurationRule(
        String code,
        RuleEffect effect,
        Map<String, String> requiresAll,
        Map<String, String> prohibitsAny,
        String message,
        String attributeCode
    ) {
        this.code = Objects.requireNonNull(code, "code");
        this.effect = Objects.requireNonNull(effect, "effect");
        this.message = Objects.requireNonNull(message, "message");
        this.requiresAll = Collections.unmodifiableMap(normalize(requiresAll));
        this.prohibitsAny = Collections.unmodifiableMap(normalize(prohibitsAny));
        this.attributeCode = attributeCode;
    }

    public String code() {
        return code;
    }

    public RuleEffect effect() {
        return effect;
    }

    public String attributeCode() {
        return attributeCode;
    }

    public Map<String, String> requiresAll() {
        return requiresAll;
    }

    public Map<String, String> prohibitsAny() {
        return prohibitsAny;
    }

    public Optional<RuleEvaluation> evaluate(ConfigurationSelection selection) {
        boolean requiresMatched = requiresAll.isEmpty() || requiresAll.entrySet().stream()
            .allMatch(entry -> entry.getValue().equals(selection.optionSelections().get(entry.getKey())));

        boolean prohibitsMatched = prohibitsAny.entrySet().stream()
            .anyMatch(entry -> entry.getValue().equals(selection.optionSelections().get(entry.getKey())));

        if (requiresMatched && prohibitsMatched) {
            return Optional.of(new RuleEvaluation(code, effect, message, attributeCode));
        }
        if (requiresMatched && prohibitsAny.isEmpty() && effect != RuleEffect.ALLOW) {
            return Optional.of(new RuleEvaluation(code, effect, message, attributeCode));
        }
        return Optional.empty();
    }

    private static Map<String, String> normalize(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return source.entrySet().stream()
            .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && !entry.getValue().isBlank())
            .collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().trim(),
                entry -> entry.getValue().trim()
            ));
    }
}
