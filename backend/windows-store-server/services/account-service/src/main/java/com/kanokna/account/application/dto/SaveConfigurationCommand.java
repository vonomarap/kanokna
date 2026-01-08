package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for saving a configuration draft.
 */
public record SaveConfigurationCommand(
    @NotNull UUID userId,
    @NotBlank String name,
    @NotNull UUID productTemplateId,
    @NotBlank String configurationSnapshot,
    String quoteSnapshot
) {
}