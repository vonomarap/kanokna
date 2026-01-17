package com.kanokna.pricing_service.domain.exception;

import com.kanokna.shared.core.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for PricingDomainErrors factory.
 */
class PricingDomainErrorsTest {

    private static final String OPERATION = "add";
    private static final String CURRENCY_ONE = "RUB";
    private static final String CURRENCY_TWO = "USD";

    private static final BigDecimal DISCOUNT_VALUE = new BigDecimal("-1");
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("150");
    private static final BigDecimal TAX_RATE = new BigDecimal("150");

    private static final Instant START_DATE = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END_DATE = Instant.parse("2025-12-31T00:00:00Z");

    @Test
    @DisplayName("currencyMismatch includes operation and both currencies")
    void currencyMismatch_includesOperationAndBothCurrencies() {
        DomainException ex = PricingDomainErrors.currencyMismatch(OPERATION, CURRENCY_ONE, CURRENCY_TWO);

        assertEquals("ERR-PRICE-CURRENCY-MISMATCH", ex.getCode());
        assertTrue(ex.getMessage().contains(OPERATION));
        assertTrue(ex.getMessage().contains(CURRENCY_ONE));
        assertTrue(ex.getMessage().contains(CURRENCY_TWO));
    }

    @Test
    @DisplayName("invalidDiscountValue includes the value")
    void invalidDiscountValue_includesValue() {
        DomainException ex = PricingDomainErrors.invalidDiscountValue(DISCOUNT_VALUE);

        assertEquals("ERR-PRICE-INVALID-DISCOUNT-VALUE", ex.getCode());
        assertTrue(ex.getMessage().contains(DISCOUNT_VALUE.toString()));
    }

    @Test
    @DisplayName("discountExceeds100 includes percentage")
    void discountExceeds100_includesPercentage() {
        DomainException ex = PricingDomainErrors.discountExceeds100(DISCOUNT_PERCENTAGE);

        assertEquals("ERR-PRICE-DISCOUNT-EXCEEDS-100", ex.getCode());
        assertTrue(ex.getMessage().contains(DISCOUNT_PERCENTAGE.toString()));
    }

    @Test
    @DisplayName("invalidDateRange includes both dates")
    void invalidDateRange_includesBothDates() {
        DomainException ex = PricingDomainErrors.invalidDateRange(START_DATE, END_DATE);

        assertEquals("ERR-PRICE-INVALID-DATE-RANGE", ex.getCode());
        assertTrue(ex.getMessage().contains(START_DATE.toString()));
        assertTrue(ex.getMessage().contains(END_DATE.toString()));
    }

    @Test
    @DisplayName("invalidTaxRate includes the rate value")
    void invalidTaxRate_includesRate() {
        DomainException ex = PricingDomainErrors.invalidTaxRate(TAX_RATE);

        assertEquals("ERR-PRICE-INVALID-TAX-RATE", ex.getCode());
        assertTrue(ex.getMessage().contains(TAX_RATE.toString()));
    }
}
