package com.kanokna.pricing.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for PromoCode entity.
 */
public final class PromoCodeId {
    private final UUID value;

    private PromoCodeId(UUID value) {
        this.value = Objects.requireNonNull(value, "PromoCodeId cannot be null");
    }

    public static PromoCodeId of(UUID value) {
        return new PromoCodeId(value);
    }

    public static PromoCodeId of(String value) {
        return new PromoCodeId(UUID.fromString(value));
    }

    public static PromoCodeId generate() {
        return new PromoCodeId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromoCodeId that = (PromoCodeId) o;
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
