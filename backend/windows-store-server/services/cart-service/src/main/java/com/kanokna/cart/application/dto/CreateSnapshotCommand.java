package com.kanokna.cart.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for creating a checkout snapshot.
 */
public record CreateSnapshotCommand(
    @NotBlank String customerId,
    boolean acknowledgePriceChanges
) {
}
