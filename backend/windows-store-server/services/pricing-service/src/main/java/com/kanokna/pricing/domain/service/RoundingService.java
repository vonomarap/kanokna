package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.Money;
import com.kanokna.pricing.domain.model.PricingDecision;
import java.math.RoundingMode;
import java.util.List;

/**
 * Domain service for currency-specific rounding.
 * Per DEC-PRICING-ROUNDING (HALF_UP, 2 decimals for RUB)
 */
public class RoundingService {
    private static final int RUB_SCALE = 2;
    private static final RoundingMode RUB_ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money round(Money amount, String currency, List<PricingDecision> decisionTrace) {
        Money rounded;
        int scale;
        RoundingMode mode;

        if ("RUB".equals(currency)) {
            scale = RUB_SCALE;
            mode = RUB_ROUNDING_MODE;
            rounded = amount.round(scale, mode);
        } else {
            scale = 2;
            mode = RoundingMode.HALF_UP;
            rounded = amount.round(scale, mode);
        }

        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-08",
            "ROUNDING",
            String.format("eventType=PRICING_STEP decision=APPLIED keyValues=mode=%s,scale=%d,before=%s,after=%s",
                mode, scale, amount.getAmount().toPlainString(), rounded.getAmount().toPlainString())
        ));

        return rounded;
    }
}