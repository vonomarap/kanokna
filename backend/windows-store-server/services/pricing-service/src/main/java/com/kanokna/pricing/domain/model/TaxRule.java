package com.kanokna.pricing.domain.model;

import com.kanokna.pricing.domain.exception.PricingDomainErrors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for regional tax configurations.
 * Per DEC-PRICING-TAX-STRATEGY (Russia VAT 20% default).
 */
public class TaxRule {
    private final TaxRuleId id;
    private final String region;
    private final String regionName;
    private final BigDecimal taxRatePercent;
    private final String taxType;
    private boolean active;
    private final Instant createdAt;

    private TaxRule(TaxRuleId id, String region, String regionName,
                   BigDecimal taxRatePercent, String taxType, boolean active,
                   Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.region = Objects.requireNonNull(region).toUpperCase();
        this.regionName = Objects.requireNonNull(regionName);
        this.taxRatePercent = Objects.requireNonNull(taxRatePercent);
        this.taxType = Objects.requireNonNull(taxType);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);

        if (taxRatePercent.compareTo(BigDecimal.ZERO) < 0 ||
            taxRatePercent.compareTo(new BigDecimal("100")) > 0) {
            throw PricingDomainErrors.invalidTaxRate(taxRatePercent);
        }
    }

    public static TaxRule create(TaxRuleId id, String region, String regionName,
                                BigDecimal taxRatePercent, String taxType) {
        return new TaxRule(id, region, regionName, taxRatePercent, taxType, true, Instant.now());
    }

    public static TaxRule createVAT(TaxRuleId id, String region, String regionName,
                                   BigDecimal taxRatePercent) {
        return new TaxRule(id, region, regionName, taxRatePercent, "VAT", true, Instant.now());
    }

    public static TaxRule restore(TaxRuleId id, String region, String regionName,
                                  BigDecimal taxRatePercent, String taxType,
                                  boolean active, Instant createdAt) {
        return new TaxRule(id, region, regionName, taxRatePercent, taxType, active, createdAt);
    }

    public Money calculateTax(Money subtotal) {
        if (!active) {
            return Money.zero(subtotal.getCurrency());
        }

        return subtotal.multiply(taxRatePercent.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters
    public TaxRuleId getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getRegionName() {
        return regionName;
    }

    public BigDecimal getTaxRatePercent() {
        return taxRatePercent;
    }

    public String getTaxType() {
        return taxType;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxRule taxRule = (TaxRule) o;
        return id.equals(taxRule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
