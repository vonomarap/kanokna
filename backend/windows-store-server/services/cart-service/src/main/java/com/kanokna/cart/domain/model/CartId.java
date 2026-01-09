package com.kanokna.cart.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for Cart aggregate.
 */
public record CartId(UUID value) {
    public CartId {
        Objects.requireNonNull(value, "CartId value cannot be null");
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }

    public static CartId of(String value) {
        return new CartId(UUID.fromString(value));
    }

    public static CartId of(UUID value) {
        return new CartId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
