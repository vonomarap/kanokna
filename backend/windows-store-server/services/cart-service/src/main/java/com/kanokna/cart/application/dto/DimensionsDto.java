package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Dimensions in centimeters.
 */
public record DimensionsDto(
    @NotNull Integer widthCm,
    @NotNull Integer heightCm
) {
}
