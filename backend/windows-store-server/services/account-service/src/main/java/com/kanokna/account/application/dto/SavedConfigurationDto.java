package com.kanokna.account.application.dto;

import java.time.Instant;

/**
 * DTO for saved configurations.
 */
public record SavedConfigurationDto(
    String configurationId,
    String name,
    String productTemplateId,
    String configurationSnapshot,
    String quoteSnapshot,
    Instant createdAt,
    Instant updatedAt
) {
}