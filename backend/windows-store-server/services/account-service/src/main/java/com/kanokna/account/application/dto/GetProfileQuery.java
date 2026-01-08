package com.kanokna.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Query for retrieving a user profile.
 */
public record GetProfileQuery(
    @NotNull UUID userId
) {
}