package com.kanokna.catalog.domain.model;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: Versioned snapshot of the catalog.
 * Immutable record for audit trail and time-travel queries.
 *
 * @param snapshot JSON representation of templates
 */
public record CatalogVersion(UUID id, int versionNumber, Instant publishedAt, String publishedBy, String snapshot) {

  public CatalogVersion(
    UUID id,
    int versionNumber,
    Instant publishedAt,
    String publishedBy,
    String snapshot
  ) {
    this.id = Objects.requireNonNull(id, "CatalogVersion id cannot be null");
    if (versionNumber <= 0) {
      throw CatalogDomainErrors.invalidVersionNumber(versionNumber);
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
