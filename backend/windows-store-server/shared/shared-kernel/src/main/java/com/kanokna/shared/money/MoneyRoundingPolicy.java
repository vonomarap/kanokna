package com.kanokna.shared.money;

import java.math.BigDecimal;

/**
 * Strategy interface for rounding monetary amounts.
 * <p>
 * Implementations define how to scale and round {@link BigDecimal} amounts
 * based on the {@link Currency}'s precision requirements.
 *
 * @see DefaultMoneyRoundingPolicy
 * @see Money
 */
public interface MoneyRoundingPolicy {

    /**
     * Rounds the given amount according to the currency's precision.
     *
     * @param amount   the amount to round
     * @param currency the currency defining the scale
     * @return the rounded amount
     * @throws IllegalArgumentException if amount or currency is null
     */
    BigDecimal round(BigDecimal amount, Currency currency);

    /**
     * Returns the default rounding policy (HALF_UP to currency scale).
     *
     * @return the default policy
     */
    static MoneyRoundingPolicy defaultPolicy() {
        return DefaultMoneyRoundingPolicy.INSTANCE;
    }
}
