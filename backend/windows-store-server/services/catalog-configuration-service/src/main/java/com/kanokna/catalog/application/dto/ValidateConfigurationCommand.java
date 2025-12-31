package com.kanokna.catalog.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Command DTO for configuration validation.
 */
public record ValidateConfigurationCommand(
    @NotNull UUID productTemplateId,
    @Min(50) @Max(400) int widthCm,
    @Min(50) @Max(400) int heightCm,
    @NotNull Map<String, UUID> selectedOptions
) {
}
