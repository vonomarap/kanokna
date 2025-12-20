package com.kanokna.catalog_configuration_service.domain.model;

import java.util.Objects;

public record ValidationMessage(
    ValidationSeverity severity,
    String code,
    String message,
    String attributeCode
) {
    public ValidationMessage {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }

    public boolean isError() {
        return severity == ValidationSeverity.ERROR;
    }
}
