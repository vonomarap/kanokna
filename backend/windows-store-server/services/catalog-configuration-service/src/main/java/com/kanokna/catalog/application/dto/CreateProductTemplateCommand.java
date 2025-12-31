package com.kanokna.catalog.application.dto;

import com.kanokna.catalog.domain.model.ProductFamily;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Command DTO for creating product template.
 */
public record CreateProductTemplateCommand(
    @NotBlank String name,
    String description,
    @NotNull ProductFamily productFamily,
    @NotNull DimensionConstraintsDto dimensionConstraints,
    List<OptionGroupDto> optionGroups
) {

    public record DimensionConstraintsDto(
        int minWidthCm,
        int maxWidthCm,
        int minHeightCm,
        int maxHeightCm
    ) {
    }

    public record OptionGroupDto(
        String name,
        boolean required,
        boolean multiSelect,
        List<OptionDto> options
    ) {
    }

    public record OptionDto(
        String name,
        String description,
        String skuCode
    ) {
    }
}
