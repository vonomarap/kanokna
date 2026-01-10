package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Command for adding a configured item to a cart.
 */
public record AddItemCommand(
    String customerId,
    String sessionId,
    @NotBlank String productTemplateId,
    @NotBlank String productName,
    String productFamily,
    String thumbnailUrl,
    @NotNull DimensionsDto dimensions,
    List<SelectedOptionDto> selectedOptions,
    @Min(1) int quantity,
    String quoteId,
    List<BomLineDto> resolvedBom
) {
}
