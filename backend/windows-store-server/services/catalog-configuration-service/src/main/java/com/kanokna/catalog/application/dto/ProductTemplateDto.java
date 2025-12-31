package com.kanokna.catalog.application.dto;

import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.TemplateStatus;

import java.util.List;
import java.util.UUID;

/**
 * DTO for product template details.
 */
public record ProductTemplateDto(
    UUID id,
    String name,
    String description,
    ProductFamily productFamily,
    DimensionConstraintsDto dimensionConstraints,
    TemplateStatus status,
    long version,
    List<OptionGroupDto> optionGroups
) {

    public record DimensionConstraintsDto(
        int minWidthCm,
        int maxWidthCm,
        int minHeightCm,
        int maxHeightCm
    ) {
    }
}
