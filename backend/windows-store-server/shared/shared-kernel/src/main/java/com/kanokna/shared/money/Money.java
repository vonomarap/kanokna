package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) throw new IllegalArgumentException("amount required");
        if (currency == null) throw new IllegalArgumentException("currency required");
        
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money ofRounded(BigDecimal amount, Currency currency, MoneyRoundingPolicy policy) {
        return new Money(policy.round(amount, currency), currency);
    }

    public BigDecimal amount() { 
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public Money add(Money other, MoneyRoundingPolicy policy) {
        requireSameCurrency(other);
        return ofRounded(this.amount.subtract(other.amount), currency, policy);
    }

    public Money multiply(BigDecimal factor, MoneyRoundingPolicy policy) {
        if (factor == null) throw new IllegalArgumentException("factor required");
        // Multiply with a safe intermediate scale then round by policy
        BigDecimal raw = this.amount.multiply(factor).setScale(8, RoundingMode.HALF_UP);
        return ofRounded(raw, currency, policy);
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (other == null || other.currency != this.currency) {
            var otherCurrency = other == null ? null : other.currency;
            throw new IllegalArgumentException("Currency mismatch: %s vs %s".formatted(this.currency, otherCurrency));
        }
    }

    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currency == money.currency;
    }

    @Override 
    public int hashCode() { 
        return Objects.hash(amount.stripTrailingZeros(), currency); 
    }

    @Override 
    public String toString() {
        return currency + " " + amount.toPlainString(); 
    }
}
