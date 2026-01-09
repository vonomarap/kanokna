package com.kanokna.pricing_service.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate for versioned price book snapshots.
 * Per DEC-ARCH-ENTITY-VERSIONING for audit trail.
 */
public class PriceBookVersion {
    private final UUID id;
    private final PriceBookId priceBookId;
    private final int versionNumber;
    private final String snapshot; // JSON snapshot
    private final Instant publishedAt;
    private final String publishedBy;

    private PriceBookVersion(UUID id, PriceBookId priceBookId, int versionNumber,
                            String snapshot, Instant publishedAt, String publishedBy) {
        this.id = Objects.requireNonNull(id);
        this.priceBookId = Objects.requireNonNull(priceBookId);
        this.versionNumber = versionNumber;
        this.snapshot = Objects.requireNonNull(snapshot);
        this.publishedAt = Objects.requireNonNull(publishedAt);
        this.publishedBy = publishedBy;

        if (versionNumber < 1) {
            throw new IllegalArgumentException("Version number must be positive");
        }
    }

    public static PriceBookVersion create(PriceBookId priceBookId, int versionNumber,
                                         String snapshot, String publishedBy) {
        return new PriceBookVersion(UUID.randomUUID(), priceBookId, versionNumber,
            snapshot, Instant.now(), publishedBy);
    }

    public UUID getId() {
        return id;
    }

    public PriceBookId getPriceBookId() {
        return priceBookId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceBookVersion that = (PriceBookVersion) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
