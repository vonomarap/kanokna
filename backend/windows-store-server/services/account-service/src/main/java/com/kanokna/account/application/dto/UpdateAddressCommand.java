package com.kanokna.account.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for updating an address.
 */
public record UpdateAddressCommand(
    @NotNull UUID userId,
    @NotNull UUID addressId,
    @Valid AddressDto address,
    String label,
    Boolean setAsDefault
) {
}