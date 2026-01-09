package com.kanokna.cart.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for CartItem entity.
 */
public record CartItemId(UUID value) {
    public CartItemId {
        Objects.requireNonNull(value, "CartItemId value cannot be null");
    }

    public static CartItemId generate() {
        return new CartItemId(UUID.randomUUID());
    }

    public static CartItemId of(String value) {
        return new CartItemId(UUID.fromString(value));
    }

    public static CartItemId of(UUID value) {
        return new CartItemId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
