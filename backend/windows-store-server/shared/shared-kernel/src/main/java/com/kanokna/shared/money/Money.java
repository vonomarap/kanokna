package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/* <FUNCTION_CONTRACT id="FC-shared-kernel-money-Money-add"
     LAYER="domain.value"
     INTENT="Add two Money amounts of the same currency"
     INPUT="Money other"
     OUTPUT="Money (new instance with sum)"
     SIDE_EFFECTS="None (immutable)"
     LINKS="RequirementsAnalysis.xml#NFR-I18N-MULTI-CURRENCY">
   <PRECONDITIONS>
     <Item>other is not null</Item>
     <Item>other.currency equals this.currency</Item>
   </PRECONDITIONS>

   <POSTCONDITIONS>
     <Item>Returned Money has same currency as inputs</Item>
     <Item>Returned amount equals this.amount + other.amount</Item>
     <Item>Original Money instances unchanged</Item>
   </POSTCONDITIONS>

   <INVARIANTS>
     <Item>Currency mismatch throws IllegalArgumentException</Item>
     <Item>Result precision matches currency's default scale</Item>
   </INVARIANTS>

   <ERROR_HANDLING>
     <Item type="TECHNICAL" code="IllegalArgumentException">Currency mismatch</Item>
     <Item type="TECHNICAL" code="NullPointerException">Null argument</Item>
   </ERROR_HANDLING>

   <TESTS>
     <Case id="TC-MONEY-001">Add two RUB amounts correctly</Case>
     <Case id="TC-MONEY-002">Add two EUR amounts correctly</Case>
     <Case id="TC-MONEY-003">Currency mismatch throws exception</Case>
     <Case id="TC-MONEY-004">Null argument throws exception</Case>
     <Case id="TC-MONEY-005">Precision maintained for currency</Case>
   </TESTS>
 </FUNCTION_CONTRACT> */

/**
 * An immutable, type-safe representation of a monetary value.
 * <p>
 * This class ensures that the internal {@link BigDecimal} amount is always stored
 * with the correct scale for its {@link Currency} (e.g., 2 for RUB, EUR, USD).
 * All arithmetic operations return a new, correctly rounded Money object.
 * <p>
 * Money is designed to be framework-free and serialization-agnostic.
 * Adapters handle serialization to/from DTOs or persistence formats.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Money price = Money.of(new BigDecimal("1500.00"), Currency.RUB);
 * Money tax = price.multiplyBy(new BigDecimal("0.20"));
 * Money total = price.add(tax);
 * }</pre>
 *
 * @see Currency
 * @see MoneyRoundingPolicy
 * @see <a href="RequirementsAnalysis.xml#NFR-I18N-MULTI-CURRENCY">NFR-I18N-MULTI-CURRENCY</a>
 */
public final class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final Currency currency;

    /**
     * Private constructor - use factory methods.
     */
    private Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Factory Methods
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Money instance with the given amount and currency,
     * using the default rounding policy.
     *
     * @param amount   the monetary amount
     * @param currency the currency
     * @return a new Money instance with scaled amount
     * @throws NullPointerException if amount or currency is null
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return of(amount, currency, MoneyRoundingPolicy.defaultPolicy());
    }

    /**
     * Creates a Money instance with the given amount, currency, and rounding policy.
     *
     * @param amount   the monetary amount
     * @param currency the currency
     * @param policy   the rounding policy to apply
     * @return a new Money instance with scaled amount
     * @throws NullPointerException if any argument is null
     */
    public static Money of(BigDecimal amount, Currency currency, MoneyRoundingPolicy policy) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        // BA-SK-MONEY-03: Apply rounding policy
        BigDecimal scaled = policy.round(amount, currency);
        return new Money(scaled, currency);
    }

    /**
     * Creates a Money instance from a whole number of major currency units.
     *
     * @param majorUnits the amount in major units (e.g., rubles, euros)
     * @param currency   the currency
     * @return a new Money instance
     */
    public static Money ofMajor(long majorUnits, Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        BigDecimal amount = BigDecimal.valueOf(majorUnits)
            .setScale(currency.getDefaultScale(), RoundingMode.UNNECESSARY);
        return new Money(amount, currency);
    }

    /**
     * Creates a Money instance from minor currency units (e.g., kopecks, cents).
     *
     * @param minorUnits the amount in minor units
     * @param currency   the currency
     * @return a new Money instance
     */
    public static Money ofMinor(long minorUnits, Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        BigDecimal divisor = BigDecimal.TEN.pow(currency.getDefaultScale());
        BigDecimal amount = BigDecimal.valueOf(minorUnits)
            .divide(divisor, currency.getDefaultScale(), RoundingMode.UNNECESSARY);
        return new Money(amount, currency);
    }

    /**
     * Creates a zero Money in the given currency.
     *
     * @param currency the currency
     * @return Money representing zero in the given currency
     */
    public static Money zero(Currency currency) {
        return ofMajor(0, currency);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns the monetary amount.
     *
     * @return the amount as BigDecimal
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the currency.
     *
     * @return the currency
     */
    public Currency getCurrency() {
        return currency;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Queries
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if this Money represents zero.
     *
     * @return true if amount is zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Returns true if this Money is positive (greater than zero).
     *
     * @return true if amount is positive
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns true if this Money is negative (less than zero).
     *
     * @return true if amount is negative
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Arithmetic Operations
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Adds another Money amount to this one.
     * Both amounts must have the same currency.
     *
     * @param other the Money to add
     * @return a new Money representing the sum
     * @throws NullPointerException     if other is null
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        return add(other, MoneyRoundingPolicy.defaultPolicy());
    }

    /**
     * Adds another Money amount with a specific rounding policy.
     * <!-- BLOCK_ANCHOR id="BA-SK-MONEY-02" purpose="Perform arithmetic operation" -->
     *
     * @param other  the Money to add
     * @param policy the rounding policy
     * @return a new Money representing the sum
     * @throws NullPointerException     if other or policy is null
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other, MoneyRoundingPolicy policy) {
        // BA-SK-MONEY-01: Validate currency compatibility
        requireSameCurrency(other);
        Objects.requireNonNull(policy, "policy must not be null");

        // BA-SK-MONEY-02: Perform arithmetic operation
        BigDecimal sum = this.amount.add(other.amount);
        return of(sum, currency, policy);
    }

    /**
     * Subtracts another Money amount from this one.
     * Both amounts must have the same currency.
     *
     * @param other the Money to subtract
     * @return a new Money representing the difference
     * @throws NullPointerException     if other is null
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        return subtract(other, MoneyRoundingPolicy.defaultPolicy());
    }

    /**
     * Subtracts another Money amount with a specific rounding policy.
     *
     * @param other  the Money to subtract
     * @param policy the rounding policy
     * @return a new Money representing the difference
     */
    public Money subtract(Money other, MoneyRoundingPolicy policy) {
        requireSameCurrency(other);
        Objects.requireNonNull(policy, "policy must not be null");

        BigDecimal difference = this.amount.subtract(other.amount);
        return of(difference, currency, policy);
    }

    /**
     * Multiplies this Money by a factor.
     *
     * @param factor the multiplication factor
     * @return a new Money representing the product
     * @throws NullPointerException if factor is null
     */
    public Money multiplyBy(BigDecimal factor) {
        return multiplyBy(factor, MoneyRoundingPolicy.defaultPolicy());
    }

    /**
     * Multiplies this Money by a factor with a specific rounding policy.
     *
     * @param factor the multiplication factor
     * @param policy the rounding policy
     * @return a new Money representing the product
     */
    public Money multiplyBy(BigDecimal factor, MoneyRoundingPolicy policy) {
        Objects.requireNonNull(factor, "factor must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        BigDecimal product = this.amount.multiply(factor);
        return of(product, currency, policy);
    }

    /**
     * Returns the negation of this Money.
     *
     * @return a new Money with negated amount
     */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /**
     * Returns the absolute value of this Money.
     *
     * @return a new Money with absolute amount
     */
    public Money abs() {
        return isNegative() ? negate() : this;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Comparison
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Compares this Money to another of the same currency.
     *
     * @param other the other Money
     * @return negative if this < other, zero if equal, positive if this > other
     * @throws IllegalArgumentException if currencies don't match
     */
    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /**
     * Returns true if this Money is greater than the other.
     *
     * @param other the other Money
     * @return true if this > other
     */
    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns true if this Money is less than the other.
     *
     * @param other the other Money
     * @return true if this < other
     */
    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Ensures the other Money has the same currency.
     * <!-- BLOCK_ANCHOR id="BA-SK-MONEY-01" purpose="Validate currency compatibility" -->
     */
    private void requireSameCurrency(Money other) {
        // BA-SK-MONEY-01: Validate currency compatibility
        Objects.requireNonNull(other, "other Money instance must not be null");

        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                "Currency mismatch: %s vs %s".formatted(this.currency, other.currency));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Object Methods
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return currency == money.currency && amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return currency.name() + " " + amount.toPlainString();
    }

    /**
     * Returns a formatted string with currency symbol.
     *
     * @return formatted string (e.g., "1,500.00 ₽")
     */
    public String toFormattedString() {
        return amount.toPlainString() + " " + currency.getSymbol();
    }
}
