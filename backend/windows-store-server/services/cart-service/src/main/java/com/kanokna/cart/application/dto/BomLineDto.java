package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Bill of materials line snapshot.
 */
public record BomLineDto(
    @NotBlank String sku,
    @NotBlank String description,
    @NotNull @Min(1) Integer quantity
) {
}
