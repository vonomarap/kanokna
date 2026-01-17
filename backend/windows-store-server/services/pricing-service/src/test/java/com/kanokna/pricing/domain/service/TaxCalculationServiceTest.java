package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.Money;
import com.kanokna.pricing.domain.model.PricingDecision;
import com.kanokna.pricing.domain.model.TaxRule;
import com.kanokna.pricing.domain.model.TaxRuleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculationServiceTest {

    @Test
    @DisplayName("TC-FUNC-CALC-009: Tax calculated on subtotal")
    void taxCalculatedOnSubtotal() {
        TaxCalculationService service = new TaxCalculationService();
        Money subtotal = Money.of(new BigDecimal("900"), "RUB");
        TaxRule rule = TaxRule.createVAT(TaxRuleId.generate(), "RU", "Russia", new BigDecimal("20"));

        Money tax = service.calculateTax(subtotal, rule, new ArrayList<>());

        assertEquals(0, new BigDecimal("180").compareTo(tax.getAmount()));
    }

    @Test
    @DisplayName("Inactive tax rule returns zero")
    void inactiveTaxRuleReturnsZero() {
        TaxCalculationService service = new TaxCalculationService();
        Money subtotal = Money.of(new BigDecimal("900"), "RUB");
        TaxRule rule = TaxRule.restore(TaxRuleId.generate(), "RU", "Russia", new BigDecimal("20"), "VAT", false, Instant.now());

        Money tax = service.calculateTax(subtotal, rule, new ArrayList<PricingDecision>());

        assertEquals(0, BigDecimal.ZERO.compareTo(tax.getAmount()));
    }
}
