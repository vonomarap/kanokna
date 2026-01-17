package com.kanokna.pricing_service.domain.model;

import com.kanokna.pricing_service.domain.exception.PricingDomainErrors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entity representing a campaign discount rule.
 * Part of Campaign aggregate.
 */
public class CampaignRule {
    private final DiscountType discountType;
    private final BigDecimal discountValue;
    private final Money maxDiscount;

    private CampaignRule(DiscountType discountType, BigDecimal discountValue, Money maxDiscount) {
        this.discountType = Objects.requireNonNull(discountType);
        this.discountValue = Objects.requireNonNull(discountValue);
        this.maxDiscount = maxDiscount;

        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw PricingDomainErrors.invalidDiscountValue(discountValue);
        }
    }

    public static CampaignRule percentage(BigDecimal percentage, Money maxDiscount) {
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw PricingDomainErrors.discountExceeds100(percentage);
        }
        return new CampaignRule(DiscountType.PERCENTAGE, percentage, maxDiscount);
    }

    public static CampaignRule fixed(Money amount) {
        return new CampaignRule(DiscountType.FIXED, amount.getAmount(), null);
    }

    public Money calculateDiscount(Money subtotal) {
        Money discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(discountValue.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        } else {
            discount = Money.of(discountValue, subtotal.getCurrency());
        }

        if (maxDiscount != null && discount.isGreaterThan(maxDiscount)) {
            return maxDiscount;
        }

        return discount;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Money getMaxDiscount() {
        return maxDiscount;
    }
}
