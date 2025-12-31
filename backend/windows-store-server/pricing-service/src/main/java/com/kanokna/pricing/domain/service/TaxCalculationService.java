package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.Money;
import com.kanokna.pricing.domain.model.PricingDecision;
import com.kanokna.pricing.domain.model.TaxRule;

import java.util.List;

/**
 * Domain service for calculating regional taxes.
 * Per DEC-PRICING-TAX-STRATEGY (Russia VAT 20% default).
 */
public class TaxCalculationService {

    public Money calculateTax(Money subtotal, TaxRule taxRule, List<PricingDecision> decisionTrace) {
        if (taxRule == null || !taxRule.isActive()) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-07",
                "TAX_CALCULATION",
                "decision=NO_TAX (no tax rule or inactive)"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        // Calculate tax on discounted subtotal
        Money tax = taxRule.calculateTax(subtotal);

        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-07",
            "TAX_CALCULATION",
            String.format("decision=CALCULATED, region=%s, rate=%.2f%%, tax=%.2f %s",
                taxRule.getRegion(),
                taxRule.getTaxRatePercent(),
                tax.getAmount(),
                subtotal.getCurrency())
        ));

        return tax;
    }
}
