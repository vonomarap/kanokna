package com.kanokna.pricing.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing a premium for a product option.
 * Supports ABSOLUTE (fixed amount) and PERCENTAGE premiums per DEC-PRICING-PREMIUM-TYPES.
 * Part of PriceBook aggregate.
 */
public class OptionPremium {
    private final String optionId;
    private final String optionName;
    private final PremiumType premiumType;
    private final BigDecimal amount;

    private OptionPremium(String optionId, String optionName, PremiumType premiumType, BigDecimal amount) {
        this.optionId = Objects.requireNonNull(optionId);
        this.optionName = Objects.requireNonNull(optionName);
        this.premiumType = Objects.requireNonNull(premiumType);
        this.amount = Objects.requireNonNull(amount);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Premium amount cannot be negative");
        }
    }

    public static OptionPremium absolute(String optionId, String optionName, Money amount) {
        return new OptionPremium(optionId, optionName, PremiumType.ABSOLUTE, amount.getAmount());
    }

    public static OptionPremium percentage(String optionId, String optionName, BigDecimal percentage) {
        return new OptionPremium(optionId, optionName, PremiumType.PERCENTAGE, percentage);
    }

    public Money calculatePremium(Money basePrice) {
        if (premiumType == PremiumType.ABSOLUTE) {
            return Money.of(amount, basePrice.getCurrency());
        } else { // PERCENTAGE
            return basePrice.multiply(amount.divide(new BigDecimal("100")));
        }
    }

    public String getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public PremiumType getPremiumType() {
        return premiumType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionPremium that = (OptionPremium) o;
        return optionId.equals(that.optionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId);
    }
}
