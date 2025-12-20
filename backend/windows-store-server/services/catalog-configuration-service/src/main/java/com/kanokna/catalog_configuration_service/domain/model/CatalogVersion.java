package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.shared.core.Id;

import java.time.Instant;
import java.util.Objects;

public final class CatalogVersion {
    private final Id id;
    private final Id tenantId;
    private final int versionNumber;
    private final CatalogVersionStatus status;
    private final Instant effectiveFrom;
    private final Instant effectiveTo;

    public CatalogVersion(
        Id id,
        Id tenantId,
        int versionNumber,
        CatalogVersionStatus status,
        Instant effectiveFrom,
        Instant effectiveTo
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("versionNumber must be positive");
        }
        this.versionNumber = versionNumber;
        this.status = Objects.requireNonNull(status, "status");
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("effectiveFrom must be before effectiveTo");
        }
    }

    public Id id() {
        return id;
    }

    public Id tenantId() {
        return tenantId;
    }

    public int versionNumber() {
        return versionNumber;
    }

    public CatalogVersionStatus status() {
        return status;
    }

    public Instant effectiveFrom() {
        return effectiveFrom;
    }

    public Instant effectiveTo() {
        return effectiveTo;
    }

    public boolean isActive() {
        return status == CatalogVersionStatus.ACTIVE;
    }

    public CatalogVersion activate(Instant publishedAt) {
        if (status == CatalogVersionStatus.ACTIVE) {
            return this;
        }
        if (status == CatalogVersionStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot activate deprecated catalog version");
        }
        return new CatalogVersion(id, tenantId, versionNumber, CatalogVersionStatus.ACTIVE, publishedAt, effectiveTo);
    }
}
