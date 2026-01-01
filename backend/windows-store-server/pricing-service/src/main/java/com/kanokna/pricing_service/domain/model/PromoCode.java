package com.kanokna.pricing_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a promotional code with usage limits.
 * Applies discounts to quotes when validated.
 */
public class PromoCode {
    private final PromoCodeId id;
    private final String code;
    private final String description;
    private final DiscountType discountType;
    private final BigDecimal discountValue;
    private final Money maxDiscount;
    private final Money minSubtotal;
    private final Integer usageLimit;
    private int usageCount;
    private final Instant startDate;
    private final Instant endDate;
    private boolean active;
    private final Instant createdAt;
    private final String createdBy;

    private PromoCode(PromoCodeId id, String code, String description, DiscountType discountType,
                     BigDecimal discountValue, Money maxDiscount, Money minSubtotal,
                     Integer usageLimit, Instant startDate, Instant endDate, String createdBy,
                     int usageCount, boolean active, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.code = Objects.requireNonNull(code).toUpperCase();
        this.description = description;
        this.discountType = Objects.requireNonNull(discountType);
        this.discountValue = Objects.requireNonNull(discountValue);
        this.maxDiscount = maxDiscount;
        this.minSubtotal = minSubtotal;
        this.usageLimit = usageLimit;
        this.usageCount = usageCount;
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.createdBy = createdBy;

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public static PromoCode create(PromoCodeId id, String code, String description,
                                   DiscountType discountType, BigDecimal discountValue,
                                   Money maxDiscount, Money minSubtotal, Integer usageLimit,
                                   Instant startDate, Instant endDate, String createdBy) {
        return new PromoCode(id, code, description, discountType, discountValue, maxDiscount,
            minSubtotal, usageLimit, startDate, endDate, createdBy, 0, true, Instant.now());
    }

    public static PromoCode restore(PromoCodeId id, String code, String description,
                                    DiscountType discountType, BigDecimal discountValue,
                                    Money maxDiscount, Money minSubtotal, Integer usageLimit,
                                    int usageCount, Instant startDate, Instant endDate,
                                    boolean active, Instant createdAt, String createdBy) {
        return new PromoCode(id, code, description, discountType, discountValue, maxDiscount,
            minSubtotal, usageLimit, startDate, endDate, createdBy, usageCount, active, createdAt);
    }

    public boolean isValid(Instant now, Money subtotal) {
        if (!active) {
            return false;
        }

        if (now.isBefore(startDate) || now.isAfter(endDate)) {
            return false;
        }

        if (usageLimit != null && usageCount >= usageLimit) {
            return false;
        }

        if (minSubtotal != null && subtotal.isLessThan(minSubtotal)) {
            return false;
        }

        return true;
    }

    public Money calculateDiscount(Money subtotal) {
        Money discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(discountValue.divide(new BigDecimal("100")));
        } else {
            discount = Money.of(discountValue, subtotal.getCurrency());
        }

        if (maxDiscount != null && discount.isGreaterThan(maxDiscount)) {
            return maxDiscount;
        }

        return discount;
    }

    public void incrementUsage() {
        if (usageLimit != null && usageCount >= usageLimit) {
            throw new IllegalStateException("Promo code usage limit exceeded");
        }
        this.usageCount++;
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters
    public PromoCodeId getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
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

    public Money getMinSubtotal() {
        return minSubtotal;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromoCode promoCode = (PromoCode) o;
        return id.equals(promoCode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
