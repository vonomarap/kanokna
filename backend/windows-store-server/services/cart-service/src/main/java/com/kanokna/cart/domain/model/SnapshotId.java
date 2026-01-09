package com.kanokna.cart.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for CartSnapshot aggregate.
 */
public record SnapshotId(UUID value) {
    public SnapshotId {
        Objects.requireNonNull(value, "SnapshotId value cannot be null");
    }

    public static SnapshotId generate() {
        return new SnapshotId(UUID.randomUUID());
    }

    public static SnapshotId of(String value) {
        return new SnapshotId(UUID.fromString(value));
    }

    public static SnapshotId of(UUID value) {
        return new SnapshotId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
