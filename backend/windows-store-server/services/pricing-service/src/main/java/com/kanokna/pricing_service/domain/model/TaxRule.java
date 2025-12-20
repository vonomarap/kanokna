package com.kanokna.pricing_service.domain.model;

import com.kanokna.shared.money.Money;
import com.kanokna.shared.money.Money;
import com.kanokna.shared.money.MoneyRoundingPolicy;

import java.math.BigDecimal;
import java.util.Objects;

public record TaxRule(String region, String productType, BigDecimal rate, String roundingPolicyCode) {
    public TaxRule {
        Objects.requireNonNull(region, "region");
        productType = productType == null ? "" : productType;
        Objects.requireNonNull(rate, "rate");
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative");
        }
        roundingPolicyCode = roundingPolicyCode == null || roundingPolicyCode.isBlank() ? "DEFAULT" : roundingPolicyCode;
    }

    public Money taxFor(Money taxable, MoneyRoundingPolicy policy) {
        MoneyRoundingPolicy appliedPolicy = policy == null ? MoneyRoundingPolicy.defaultPolicy() : policy;
        return taxable.multiplyBy(rate, appliedPolicy);
    }
}
