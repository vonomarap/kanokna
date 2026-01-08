package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for deleting an address.
 */
public record DeleteAddressCommand(
    @NotNull UUID userId,
    @NotNull UUID addressId
) {
}