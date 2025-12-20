package com.kanokna.pricing_service.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

public record TaxRuleCommand(
    String region,
    String productType,
    BigDecimal rate,
    String roundingPolicyCode
) {
    public TaxRuleCommand {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(rate, "rate");
    }
}
