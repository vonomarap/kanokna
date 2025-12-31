package com.kanokna.pricing.domain.model;

import java.util.Objects;

/**
 * Value object representing a single option premium in a quote.
 * Immutable component of the quote breakdown.
 */
public final class PremiumLine {
    private final String optionId;
    private final String optionName;
    private final Money amount;

    private PremiumLine(String optionId, String optionName, Money amount) {
        this.optionId = Objects.requireNonNull(optionId, "Option ID cannot be null");
        this.optionName = Objects.requireNonNull(optionName, "Option name cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
    }

    public static PremiumLine of(String optionId, String optionName, Money amount) {
        return new PremiumLine(optionId, optionName, amount);
    }

    public String getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public Money getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiumLine that = (PremiumLine) o;
        return optionId.equals(that.optionId) &&
               optionName.equals(that.optionName) &&
               amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, optionName, amount);
    }

    @Override
    public String toString() {
        return optionName + ": " + amount;
    }
}
