package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for merging anonymous cart into authenticated cart.
 */
public record MergeCartsCommand(
    @NotBlank String customerId,
    @NotBlank String anonymousSessionId
) {
}
