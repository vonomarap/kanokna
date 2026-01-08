package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Query for listing saved configurations.
 */
public record ListConfigurationsQuery(
    @NotNull UUID userId
) {
}