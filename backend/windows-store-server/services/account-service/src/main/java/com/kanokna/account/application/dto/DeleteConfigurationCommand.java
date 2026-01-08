package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for deleting a saved configuration.
 */
public record DeleteConfigurationCommand(
    @NotNull UUID userId,
    @NotNull UUID configurationId
) {
}