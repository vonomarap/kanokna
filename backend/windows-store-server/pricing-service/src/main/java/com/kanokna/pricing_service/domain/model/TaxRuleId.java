package com.kanokna.pricing_service.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identifier for TaxRule aggregate.
 */
public final class TaxRuleId {
    private final UUID value;

    private TaxRuleId(UUID value) {
        this.value = Objects.requireNonNull(value, "TaxRuleId cannot be null");
    }

    public static TaxRuleId of(UUID value) {
        return new TaxRuleId(value);
    }

    public static TaxRuleId of(String value) {
        return new TaxRuleId(UUID.fromString(value));
    }

    public static TaxRuleId generate() {
        return new TaxRuleId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxRuleId taxRuleId = (TaxRuleId) o;
        return value.equals(taxRuleId.value);
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
