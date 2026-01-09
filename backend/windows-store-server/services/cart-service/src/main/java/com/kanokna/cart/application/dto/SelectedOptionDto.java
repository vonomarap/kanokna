package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Selected option from an option group.
 */
public record SelectedOptionDto(
    @NotBlank String optionGroupId,
    @NotBlank String optionId
) {
}
