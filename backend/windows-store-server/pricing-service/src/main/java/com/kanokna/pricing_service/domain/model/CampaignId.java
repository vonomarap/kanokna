package com.kanokna.pricing_service.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for Campaign aggregate.
 */
public final class CampaignId {
    private final UUID value;

    private CampaignId(UUID value) {
        this.value = Objects.requireNonNull(value, "CampaignId cannot be null");
    }

    public static CampaignId of(UUID value) {
        return new CampaignId(value);
    }

    public static CampaignId of(String value) {
        return new CampaignId(UUID.fromString(value));
    }

    public static CampaignId generate() {
        return new CampaignId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampaignId that = (CampaignId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
