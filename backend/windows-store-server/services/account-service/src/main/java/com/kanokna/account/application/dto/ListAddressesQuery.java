package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Query for listing addresses.
 */
public record ListAddressesQuery(
    @NotNull UUID userId
) {
}