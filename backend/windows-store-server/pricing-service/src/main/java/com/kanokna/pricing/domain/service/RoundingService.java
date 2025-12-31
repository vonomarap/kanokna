package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.Money;
import com.kanokna.pricing.domain.model.PricingDecision;

import java.math.RoundingMode;
import java.util.List;

/**
 * Domain service for currency-specific rounding.
 * Per DEC-PRICING-ROUNDING (HALF_UP, 2 decimals for RUB).
 */
public class RoundingService {

    private static final int RUB_SCALE = 2;
    private static final RoundingMode RUB_ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money round(Money amount, String currency, List<PricingDecision> decisionTrace) {
        Money rounded;

        if ("RUB".equals(currency)) {
            rounded = amount.round(RUB_SCALE, RUB_ROUNDING_MODE);
        } else {
            // Default rounding for other currencies
            rounded = amount.round(2, RoundingMode.HALF_UP);
        }

        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-08",
            "ROUNDING",
            String.format("decision=APPLIED, mode=%s, scale=%d, before=%.10f, after=%.2f",
                RUB_ROUNDING_MODE, RUB_SCALE,
                amount.getAmount(), rounded.getAmount())
        ));

        return rounded;
    }
}
