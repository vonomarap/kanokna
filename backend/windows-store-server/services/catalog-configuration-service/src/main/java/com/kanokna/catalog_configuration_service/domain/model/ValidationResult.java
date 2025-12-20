package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.catalog_configuration_service.domain.event.ConfigurationValidatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ValidationResult {
    private final List<ValidationMessage> errors;
    private final List<ValidationMessage> warnings;
    private final List<DecisionTrace> traces;
    private final ConfigurationValidatedEvent validatedEvent;

    private ValidationResult(
        List<ValidationMessage> errors,
        List<ValidationMessage> warnings,
        List<DecisionTrace> traces,
        ConfigurationValidatedEvent validatedEvent
    ) {
        this.errors = Collections.unmodifiableList(List.copyOf(errors));
        this.warnings = Collections.unmodifiableList(List.copyOf(warnings));
        this.traces = Collections.unmodifiableList(List.copyOf(traces));
        this.validatedEvent = validatedEvent;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationMessage> errors() {
        return errors;
    }

    public List<ValidationMessage> warnings() {
        return warnings;
    }

    public List<DecisionTrace> traces() {
        return traces;
    }

    public Optional<ConfigurationValidatedEvent> validatedEvent() {
        return Optional.ofNullable(validatedEvent);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ValidationMessage> errors = new ArrayList<>();
        private final List<ValidationMessage> warnings = new ArrayList<>();
        private final List<DecisionTrace> traces = new ArrayList<>();
        private ConfigurationValidatedEvent validatedEvent;

        public Builder addError(String code, String message, String attributeCode) {
            errors.add(new ValidationMessage(ValidationSeverity.ERROR, code, message, attributeCode));
            return this;
        }

        public Builder addWarning(String code, String message, String attributeCode) {
            warnings.add(new ValidationMessage(ValidationSeverity.WARNING, code, message, attributeCode));
            return this;
        }

        public Builder trace(String blockId, String state, String detail) {
            traces.add(new DecisionTrace(blockId, state, detail));
            return this;
        }

        public Builder validatedEvent(ConfigurationValidatedEvent event) {
            this.validatedEvent = event;
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(errors, warnings, traces, validatedEvent);
        }
    }
}
