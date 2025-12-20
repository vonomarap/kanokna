package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Currency;

/**
 * An immutable, type-safe representation of a monetary value.
 *
 * This class ensures that the internal BigDecimal amount is always stored
 * with the correct scale for its currency (e.g., 2 for USD, 0 for JPY).
 * All arithmetic operations return a new, correctly rounded Money object.
 *
 * @see MoneyRoundingPolicy
 */

public final class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    // --- FACTORY METHODS ---

    /*<FUNCTION_CONTRACT
        id="money.of.defaultPolicy"
        module="mod.shared.kernel"
        SPECIFICATION="RequirementsAnalysis.xml#PRICING.CURRENCY"
        LINKS="MODULE_CONTRACT#mod.shared.kernel,Technology.xml#MoneyTime">
      <ROLE_IN_MODULE>
        Factory method that creates a Money instance using the default rounding policy to enforce currency-scale normalization.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - amount:BigDecimal (non-null) representing major+minor units.
          - currency:java.util.Currency (non-null) defining scale and ISO code.
        </INPUT>
        <OUTPUT>
          - Immutable Money with amount scaled per currency and policy.
        </OUTPUT>
        <SIDE_EFFECTS>
          - None; pure factory.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - amount and currency must be non-null.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Returned Money amount has scale matching currency default fraction digits after default policy applied.
        - Currency of result equals input currency.
      </POSTCONDITIONS>
      <INVARIANTS>
        - Money instances are immutable; no state mutation after construction.
      </INVARIANTS>
      <ERROR_HANDLING>
        - Null inputs throw NullPointerException or IllegalArgumentException via policy.
      </ERROR_HANDLING>
      <LOGGING>
        - None; factory remains side-effect free.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - amount=10.125, currency=USD -> rounds to 10.13 with HALF_UP.
          - amount=100, currency=JPY -> returns scale 0.
        </HAPPY_PATH>
        <EDGE_CASES>
          - amount=null -> NullPointerException.
          - currency=null -> NullPointerException.
          - policy adjusts amount to currency scale (e.g., CHF with 2 decimals).
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable; no auth context.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    public static Money of(BigDecimal amount, Currency currency) {
        return of(amount, currency, MoneyRoundingPolicy.defaultPolicy());
    }

    public static Money of(BigDecimal amount, Currency currency, MoneyRoundingPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        return new Money(policy.round(amount, currency), currency);
    }

    public static Money ofMajor(long majorUnits, Currency currency) {
        return new Money(BigDecimal.valueOf(majorUnits), currency);
    }

    public static Money zero(Currency currency) {
        // Using ofMajor for efficiency and to ensure correct scale if default were 0
        return ofMajor(0, currency);
    }

    // --- GETTERS ---

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    // --- QUERIES ---

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // --- ARITHMETIC OPERATIONS ---

    /*<FUNCTION_CONTRACT
        id="money.add.withPolicy"
        module="mod.shared.kernel"
        SPECIFICATION="RequirementsAnalysis.xml#PRICING.FORMULA"
        LINKS="MODULE_CONTRACT#mod.shared.kernel,Technology.xml#MoneyTime">
      <ROLE_IN_MODULE>
        Adds two Money amounts of the same currency, returning a new rounded Money per policy.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - other:Money (non-null) with same currency.
          - policy:MoneyRoundingPolicy used to normalize the result.
        </INPUT>
        <OUTPUT>
          - New Money representing sum with correct scale.
        </OUTPUT>
        <SIDE_EFFECTS>
          - None; returns new instance.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - other is non-null and currency matches this.currency.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Result currency equals operands’ currency.
        - Result amount equals this.amount + other.amount after rounding policy.
      </POSTCONDITIONS>
      <INVARIANTS>
        - No mutation of either operand.
        - Currency mismatch results in IllegalArgumentException.
      </INVARIANTS>
      <ERROR_HANDLING>
        - Null other -> NullPointerException.
        - Currency mismatch -> IllegalArgumentException.
      </ERROR_HANDLING>
      <LOGGING>
        - None; arithmetic remains pure.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - USD 10.10 + USD 5.15 = USD 15.25.
        </HAPPY_PATH>
        <EDGE_CASES>
          - Currency mismatch (USD vs EUR) -> IllegalArgumentException.
          - Adding zero -> returns same amount.
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    public Money add(Money other) {
        return add(other, MoneyRoundingPolicy.defaultPolicy());
    }

    public Money add(Money other, MoneyRoundingPolicy policy) {
        requireSameCurrency(other);
        return of(this.amount.add(other.amount), currency, policy);
    }

    /*<FUNCTION_CONTRACT
        id="money.multiply.withPolicy"
        module="mod.shared.kernel"
        SPECIFICATION="RequirementsAnalysis.xml#PRICING.FORMULA"
        LINKS="MODULE_CONTRACT#mod.shared.kernel,Technology.xml#MoneyTime">
      <ROLE_IN_MODULE>
        Multiplies a Money amount by a factor and rounds the result according to policy.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - factor:BigDecimal (non-null) scalar.
          - policy:MoneyRoundingPolicy to normalize result.
        </INPUT>
        <OUTPUT>
          - New Money with amount multiplied and rounded; currency unchanged.
        </OUTPUT>
        <SIDE_EFFECTS>
          - None; returns new instance.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - factor is non-null.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Result currency equals this.currency; amount scaled per policy.
      </POSTCONDITIONS>
      <INVARIANTS>
        - Negative factors allowed; caller interprets business meaning (e.g., discounts).
        - Immutability preserved.
      </INVARIANTS>
      <ERROR_HANDLING>
        - Null factor -> NullPointerException.
      </ERROR_HANDLING>
      <LOGGING>
        - None; pure computation.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - USD 10.00 * 0.15 -> USD 1.50 with HALF_UP.
        </HAPPY_PATH>
        <EDGE_CASES>
          - factor=0 -> returns zero amount same currency.
          - factor negative -> returns negative Money allowed for adjustments.
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    public Money multiplyBy(BigDecimal factor) {
        return multiplyBy(factor, MoneyRoundingPolicy.defaultPolicy());
    }
    public Money multiplyBy(BigDecimal factor, MoneyRoundingPolicy policy) {
        Objects.requireNonNull(factor, "factor must not be null");

        BigDecimal newAmount = this.amount.multiply(factor);
        return of(newAmount, currency, policy);
    }

    public Money subtract(Money other) {
      return subtract(other, MoneyRoundingPolicy.defaultPolicy());
    }
    public Money subtract(Money other, MoneyRoundingPolicy policy) {
        requireSameCurrency(other);
        return of(this.amount.subtract(other.amount), currency, policy);
    }

    // --- COMPARISON ---

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other Money instance must not be null");

        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
              "Currency mismatch: %s vs %s".formatted(this.currency, other.currency));
        }
    }

    @Override 
    public boolean equals(Object o) {
        if (this == o)
          return true;
        if (!(o instanceof Money money))
          return false;
        return currency.equals(money.currency) && amount.compareTo(money.amount) == 0;
    }

    @Override 
    public int hashCode() { 
        return Objects.hash(amount.stripTrailingZeros(), currency); 
    }

    @Override 
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
