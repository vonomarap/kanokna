package com.kanokna.account.domain.model;

import com.kanokna.shared.core.Id;

import java.time.Instant;

/**
 * Saved configuration aggregate root.
 */
public record SavedConfiguration(
    Id configurationId,
    Id userId,
    String name,
    Id productTemplateId,
    ConfigurationSnapshot configurationSnapshot,
    QuoteSnapshot quoteSnapshot,
    Instant createdAt,
    Instant updatedAt
) {
}