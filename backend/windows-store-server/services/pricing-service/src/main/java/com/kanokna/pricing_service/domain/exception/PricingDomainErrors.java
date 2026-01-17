package com.kanokna.pricing_service.domain.exception;

import com.kanokna.shared.core.DomainException;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * MODULE_CONTRACT id="MC-pricing-domain-errors"
 * LAYER="domain.exception"
 * INTENT="Static factory for pricing-service domain exceptions"
 * LINKS="Technology.xml#DEC-DOMAIN-ERROR-PATTERN;RequirementsAnalysis.xml#NFR-CODE-DOMAIN-ERROR-HANDLING"
 *
 * Factory methods for pricing domain error codes.
 * All pricing exceptions should be created through this class to ensure
 * consistent error codes and messages.
 *
 * @see DomainException
 */
public final class PricingDomainErrors {

    private PricingDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    // ========== Money Operations Errors ==========

    /* <FUNCTION_CONTRACT id="FC-pricing-PricingDomainErrors-currencyMismatch"
         LAYER="domain.exception"
         INTENT="Create exception for money operations with mismatched currencies"
         INPUT="operation (String), currency1 (String), currency2 (String)"
         OUTPUT="DomainException with ERR-PRICE-CURRENCY-MISMATCH code"
         ERROR_CODE="ERR-PRICE-CURRENCY-MISMATCH"
         LINKS="MC-pricing-domain-errors">
    </FUNCTION_CONTRACT> */
    public static DomainException currencyMismatch(String operation, String currency1, String currency2) {
        return new DomainException("ERR-PRICE-CURRENCY-MISMATCH",
            String.format("Cannot %s money with different currencies: %s vs %s",
                operation, currency1, currency2));
    }

    // ========== Discount/Campaign Validation Errors ==========

    public static DomainException invalidDiscountValue(BigDecimal value) {
        return new DomainException("ERR-PRICE-INVALID-DISCOUNT-VALUE",
            "Discount value must be positive, got: " + value);
    }

    public static DomainException discountExceeds100(BigDecimal percentage) {
        return new DomainException("ERR-PRICE-DISCOUNT-EXCEEDS-100",
            "Percentage discount cannot exceed 100%, got: " + percentage);
    }

    public static DomainException invalidPremiumAmount(BigDecimal amount) {
        return new DomainException("ERR-PRICE-INVALID-PREMIUM",
            "Premium amount cannot be negative, got: " + amount);
    }

    // ========== Date Range Validation Errors ==========

    public static DomainException invalidDateRange(Instant startDate, Instant endDate) {
        return new DomainException("ERR-PRICE-INVALID-DATE-RANGE",
            String.format("End date %s must be after start date %s", endDate, startDate));
    }

    // ========== Tax Validation Errors ==========

    public static DomainException invalidTaxRate(BigDecimal rate) {
        return new DomainException("ERR-PRICE-INVALID-TAX-RATE",
            String.format("Tax rate must be between 0 and 100, got: %s", rate));
    }

    // ========== Price/Version Validation Errors ==========

    public static DomainException invalidPricePerM2(BigDecimal price) {
        return new DomainException("ERR-PRICE-INVALID-PRICE-PER-M2",
            "Price per m2 must be positive, got: " + price);
    }

    public static DomainException invalidVersionNumber(int version) {
        return new DomainException("ERR-PRICE-INVALID-VERSION",
            "versionNumber must be positive, got: " + version);
    }
}
