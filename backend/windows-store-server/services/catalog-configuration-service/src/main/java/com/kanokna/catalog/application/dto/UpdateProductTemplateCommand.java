package com.kanokna.catalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command DTO for updating product template.
 */
public record UpdateProductTemplateCommand(
    @NotNull UUID productTemplateId,
    @NotBlank String name,
    String description,
    @NotNull DimensionConstraintsDto dimensionConstraints,
    long expectedVersion
) {

    public record DimensionConstraintsDto(
        int minWidthCm,
        int maxWidthCm,
        int minHeightCm,
        int maxHeightCm
    ) {
    }
}
