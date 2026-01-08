package com.kanokna.account.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for adding a new address.
 */
public record AddAddressCommand(
    @NotNull UUID userId,
    @NotNull @Valid AddressDto address,
    @NotBlank String label,
    boolean setAsDefault
) {
}