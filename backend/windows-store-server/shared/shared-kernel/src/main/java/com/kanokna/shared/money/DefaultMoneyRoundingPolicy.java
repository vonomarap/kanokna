package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Default rounding policy that uses HALF_UP rounding to the currency's default scale.
 * <p>
 * This is the standard rounding policy for financial calculations, rounding
 * 0.5 and above up, and below 0.5 down.
 *
 * @see MoneyRoundingPolicy
 */
final class DefaultMoneyRoundingPolicy implements MoneyRoundingPolicy {

    /**
     * Singleton instance.
     */
    static final MoneyRoundingPolicy INSTANCE = new DefaultMoneyRoundingPolicy();

    private DefaultMoneyRoundingPolicy() {
        // Singleton
    }

    /**
     * Rounds the amount to the currency's default scale using HALF_UP rounding.
     * <!-- BLOCK_ANCHOR id="BA-SK-MONEY-03" purpose="Apply rounding policy" -->
     *
     * @param amount   the amount to round
     * @param currency the currency
     * @return the rounded amount
     * @throws IllegalArgumentException if amount or currency is null
     */
    @Override
    public BigDecimal round(BigDecimal amount, Currency currency) {
        // BA-SK-MONEY-03: Apply rounding policy
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("amount and currency are required");
        }

        int scale = currency.getDefaultScale();
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }
}
