package com.kanokna.pricing_service.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing base price per square meter for a product template.
 * Part of PriceBook aggregate.
 */
public class BasePriceEntry {
    private final String productTemplateId;
    private final BigDecimal pricePerM2;
    private final BigDecimal minimumAreaM2;
    private final Money minimumCharge;

    private BasePriceEntry(String productTemplateId, BigDecimal pricePerM2,
                          BigDecimal minimumAreaM2, Money minimumCharge) {
        this.productTemplateId = Objects.requireNonNull(productTemplateId);
        this.pricePerM2 = Objects.requireNonNull(pricePerM2);
        this.minimumAreaM2 = minimumAreaM2 != null ? minimumAreaM2 : new BigDecimal("0.25");
        this.minimumCharge = minimumCharge;

        if (pricePerM2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per mВІ must be positive");
        }
    }

    public static BasePriceEntry of(String productTemplateId, BigDecimal pricePerM2,
                                    BigDecimal minimumAreaM2, Money minimumCharge) {
        return new BasePriceEntry(productTemplateId, pricePerM2, minimumAreaM2, minimumCharge);
    }

    public Money calculateBasePrice(BigDecimal areaM2, String currency) {
        BigDecimal chargeableArea = areaM2.max(minimumAreaM2);
        return Money.of(chargeableArea.multiply(pricePerM2), currency);
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public BigDecimal getPricePerM2() {
        return pricePerM2;
    }

    public BigDecimal getMinimumAreaM2() {
        return minimumAreaM2;
    }

    public Money getMinimumCharge() {
        return minimumCharge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasePriceEntry that = (BasePriceEntry) o;
        return productTemplateId.equals(that.productTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productTemplateId);
    }
}

