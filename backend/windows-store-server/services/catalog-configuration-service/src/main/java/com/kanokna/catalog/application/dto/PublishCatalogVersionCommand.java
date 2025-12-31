package com.kanokna.catalog.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Command DTO for publishing catalog version.
 */
public record PublishCatalogVersionCommand(
    @NotNull String publishedBy,
    List<UUID> productTemplateIds  // null or empty = publish all DRAFT templates
) {
}
