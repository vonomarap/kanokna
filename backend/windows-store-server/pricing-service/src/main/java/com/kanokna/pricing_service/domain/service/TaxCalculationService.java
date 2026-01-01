package com.kanokna.pricing_service.domain.service;

import com.kanokna.pricing_service.domain.model.Money;
import com.kanokna.pricing_service.domain.model.PricingDecision;
import com.kanokna.pricing_service.domain.model.TaxRule;
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
                "TAX",
                "eventType=PRICING_STEP decision=NONE keyValues=region=NONE,tax_rate_pct=0,tax_rub=0"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        Money tax = taxRule.calculateTax(subtotal);

        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-07",
            "TAX",
            String.format("eventType=PRICING_STEP decision=CALCULATED keyValues=region=%s,tax_rate_pct=%s,tax_rub=%s",
                taxRule.getRegion(),
                taxRule.getTaxRatePercent().toPlainString(),
                tax.getAmount().toPlainString())
        ));

        return tax;
    }
}
