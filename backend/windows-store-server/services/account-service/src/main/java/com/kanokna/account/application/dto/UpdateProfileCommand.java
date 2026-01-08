package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for updating profile details.
 */
public record UpdateProfileCommand(
    @NotNull UUID userId,
    String firstName,
    String lastName,
    String phoneNumber,
    String preferredLanguage,
    String preferredCurrency
) {
}