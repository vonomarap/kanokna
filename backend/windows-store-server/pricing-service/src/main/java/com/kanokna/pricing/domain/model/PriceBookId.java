package com.kanokna.pricing.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for PriceBook aggregate.
 */
public final class PriceBookId {
    private final UUID value;

    private PriceBookId(UUID value) {
        this.value = Objects.requireNonNull(value, "PriceBookId cannot be null");
    }

    public static PriceBookId of(UUID value) {
        return new PriceBookId(value);
    }

    public static PriceBookId of(String value) {
        return new PriceBookId(UUID.fromString(value));
    }

    public static PriceBookId generate() {
        return new PriceBookId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceBookId that = (PriceBookId) o;
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
