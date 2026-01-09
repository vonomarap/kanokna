package com.kanokna.pricing_service.domain.model;

import java.util.Objects;

/**
 * Value object representing a single decision step in the pricing calculation.
 * Used for audit trail and decision trace.
 */
public final class PricingDecision {
    private final String step;
    private final String ruleApplied;
    private final String result;

    private PricingDecision(String step, String ruleApplied, String result) {
        this.step = Objects.requireNonNull(step, "Step cannot be null");
        this.ruleApplied = Objects.requireNonNull(ruleApplied, "Rule applied cannot be null");
        this.result = Objects.requireNonNull(result, "Result cannot be null");
    }

    public static PricingDecision of(String step, String ruleApplied, String result) {
        return new PricingDecision(step, ruleApplied, result);
    }

    public String getStep() {
        return step;
    }

    public String getRuleApplied() {
        return ruleApplied;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricingDecision that = (PricingDecision) o;
        return step.equals(that.step) &&
               ruleApplied.equals(that.ruleApplied) &&
               result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(step, ruleApplied, result);
    }

    @Override
    public String toString() {
        return step + " -> " + ruleApplied + " = " + result;
    }
}
