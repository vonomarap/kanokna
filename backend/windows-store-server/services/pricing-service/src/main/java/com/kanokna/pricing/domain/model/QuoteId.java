package com.kanokna.pricing.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for Quote value object.
 */
public final class QuoteId {
    private final UUID value;

    private QuoteId(UUID value) {
        this.value = Objects.requireNonNull(value, "QuoteId cannot be null");
    }

    public static QuoteId of(UUID value) {
        return new QuoteId(value);
    }

    public static QuoteId of(String value) {
        return new QuoteId(UUID.fromString(value));
    }

    public static QuoteId generate() {
        return new QuoteId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteId quoteId = (QuoteId) o;
        return value.equals(quoteId.value);
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
