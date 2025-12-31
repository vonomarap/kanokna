package com.kanokna.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for ProductTemplate aggregate.
 * Immutable value object.
 */
public record ProductTemplateId(UUID value) {

    public ProductTemplateId {
        Objects.requireNonNull(value, "ProductTemplateId value cannot be null");
    }

    public static ProductTemplateId generate() {
        return new ProductTemplateId(UUID.randomUUID());
    }

    public static ProductTemplateId of(String value) {
        return new ProductTemplateId(UUID.fromString(value));
    }

    public static ProductTemplateId of(UUID value) {
        return new ProductTemplateId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
