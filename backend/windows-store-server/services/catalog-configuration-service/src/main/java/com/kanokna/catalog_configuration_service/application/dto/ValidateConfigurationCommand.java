package com.kanokna.catalog_configuration_service.application.dto;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.measure.DimensionsCm;

import java.util.Map;
import java.util.Objects;

public record ValidateConfigurationCommand(
    Id templateId,
    Id tenantId,
    DimensionsCm dimensions,
    Map<String, String> optionSelections,
    String locale,
    long catalogVersion
) {
    public ValidateConfigurationCommand {
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(optionSelections, "optionSelections");
        if (locale == null || locale.isBlank()) {
            throw new IllegalArgumentException("locale must be provided");
        }
    }
}
