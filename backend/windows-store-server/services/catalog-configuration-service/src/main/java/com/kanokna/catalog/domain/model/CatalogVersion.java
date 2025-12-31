package com.kanokna.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: Versioned snapshot of the catalog.
 * Immutable record for audit trail and time-travel queries.
 */
public class CatalogVersion {

    private final UUID id;
    private final int versionNumber;
    private final Instant publishedAt;
    private final String publishedBy;
    private final String snapshot;  // JSON representation of templates

    public CatalogVersion(
        UUID id,
        int versionNumber,
        Instant publishedAt,
        String publishedBy,
        String snapshot
    ) {
        this.id = Objects.requireNonNull(id, "CatalogVersion id cannot be null");
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("versionNumber must be positive");
        }
        this.versionNumber = versionNumber;
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt cannot be null");
        this.publishedBy = publishedBy;
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null");
    }

    public static CatalogVersion create(int versionNumber, String publishedBy, String snapshot) {
        return new CatalogVersion(
            UUID.randomUUID(),
            versionNumber,
            Instant.now(),
            publishedBy,
            snapshot
        );
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public String getSnapshot() {
        return snapshot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogVersion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
