package com.kanokna.shared.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Money}.
 * <p>
 * Test cases cover contracts from FC-shared-kernel-money-Money-add:
 * - TC-MONEY-001: Add two RUB amounts correctly
 * - TC-MONEY-002: Add two EUR amounts correctly
 * - TC-MONEY-003: Currency mismatch throws exception
 * - TC-MONEY-004: Null argument throws exception
 * - TC-MONEY-005: Precision maintained for currency
 */
@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("of() creates Money with correct amount and currency")
        void ofCreatesMoneyWithCorrectAmountAndCurrency() {
            Money money = Money.of(new BigDecimal("100.50"), Currency.RUB);

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
            assertThat(money.getCurrency()).isEqualTo(Currency.RUB);
        }

        @Test
        @DisplayName("of() rounds to currency scale with HALF_UP")
        void ofRoundsToCurrencyScale() {
            // 100.125 should round to 100.13 for 2-decimal currency
            Money money = Money.of(new BigDecimal("100.125"), Currency.RUB);

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.13"));
        }

        @Test
        @DisplayName("ofMajor() creates Money from whole units")
        void ofMajorCreatesFromWholeUnits() {
            Money money = Money.ofMajor(100, Currency.EUR);

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(money.getCurrency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("ofMinor() creates Money from minor units")
        void ofMinorCreatesFromMinorUnits() {
            // 15050 kopecks = 150.50 rubles
            Money money = Money.ofMinor(15050, Currency.RUB);

            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("150.50"));
        }

        @Test
        @DisplayName("zero() creates zero Money")
        void zeroCreatesZeroMoney() {
            Money money = Money.zero(Currency.USD);

            assertThat(money.isZero()).isTrue();
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("of() throws on null amount")
        void ofThrowsOnNullAmount() {
            assertThatNullPointerException()
                .isThrownBy(() -> Money.of(null, Currency.RUB))
                .withMessageContaining("amount");
        }

        @Test
        @DisplayName("of() throws on null currency")
        void ofThrowsOnNullCurrency() {
            assertThatNullPointerException()
                .isThrownBy(() -> Money.of(BigDecimal.TEN, null))
                .withMessageContaining("currency");
        }
    }

    @Nested
    @DisplayName("Addition (TC-MONEY-001, TC-MONEY-002)")
    class Addition {

        @Test
        @DisplayName("TC-MONEY-001: Add two RUB amounts correctly")
        void addTwoRubAmountsCorrectly() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("50.50"), Currency.RUB);

            Money result = m1.add(m2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.50"));
            assertThat(result.getCurrency()).isEqualTo(Currency.RUB);
        }

        @Test
        @DisplayName("TC-MONEY-002: Add two EUR amounts correctly")
        void addTwoEurAmountsCorrectly() {
            Money m1 = Money.of(new BigDecimal("200.00"), Currency.EUR);
            Money m2 = Money.of(new BigDecimal("75.25"), Currency.EUR);

            Money result = m1.add(m2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("275.25"));
            assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("Adding zero returns same amount")
        void addingZeroReturnsSameAmount() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.USD);
            Money zero = Money.zero(Currency.USD);

            Money result = m1.add(zero);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Currency Mismatch (TC-MONEY-003)")
    class CurrencyMismatch {

        @Test
        @DisplayName("TC-MONEY-003: Currency mismatch throws IllegalArgumentException")
        void currencyMismatchThrowsException() {
            Money rub = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money eur = Money.of(new BigDecimal("50.00"), Currency.EUR);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> rub.add(eur))
                .withMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("Currency mismatch on subtraction throws exception")
        void currencyMismatchOnSubtractionThrows() {
            Money usd = Money.of(new BigDecimal("100.00"), Currency.USD);
            Money rub = Money.of(new BigDecimal("50.00"), Currency.RUB);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> usd.subtract(rub))
                .withMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("Currency mismatch on comparison throws exception")
        void currencyMismatchOnComparisonThrows() {
            Money eur = Money.of(new BigDecimal("100.00"), Currency.EUR);
            Money usd = Money.of(new BigDecimal("100.00"), Currency.USD);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> eur.compareTo(usd))
                .withMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("Null Argument (TC-MONEY-004)")
    class NullArgument {

        @Test
        @DisplayName("TC-MONEY-004: Null argument on add throws NullPointerException")
        void nullArgumentOnAddThrows() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);

            assertThatNullPointerException()
                .isThrownBy(() -> m1.add(null))
                .withMessageContaining("null");
        }

        @Test
        @DisplayName("Null argument on subtract throws NullPointerException")
        void nullArgumentOnSubtractThrows() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);

            assertThatNullPointerException()
                .isThrownBy(() -> m1.subtract(null));
        }

        @Test
        @DisplayName("Null factor on multiplyBy throws NullPointerException")
        void nullFactorOnMultiplyThrows() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);

            assertThatNullPointerException()
                .isThrownBy(() -> m1.multiplyBy(null))
                .withMessageContaining("factor");
        }
    }

    @Nested
    @DisplayName("Precision (TC-MONEY-005)")
    class Precision {

        @Test
        @DisplayName("TC-MONEY-005: Precision maintained after addition")
        void precisionMaintainedAfterAddition() {
            Money m1 = Money.of(new BigDecimal("10.10"), Currency.USD);
            Money m2 = Money.of(new BigDecimal("5.15"), Currency.USD);

            Money result = m1.add(m2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("15.25"));
            assertThat(result.getAmount().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Precision maintained after multiplication")
        void precisionMaintainedAfterMultiplication() {
            Money m1 = Money.of(new BigDecimal("10.00"), Currency.USD);

            Money result = m1.multiplyBy(new BigDecimal("0.15"));

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1.50"));
            assertThat(result.getAmount().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Rounding applied correctly on multiplication")
        void roundingAppliedOnMultiplication() {
            Money m1 = Money.of(new BigDecimal("10.00"), Currency.USD);

            // 10 * 0.333 = 3.33 (rounded from 3.330)
            Money result = m1.multiplyBy(new BigDecimal("0.333"));

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("3.33"));
        }
    }

    @Nested
    @DisplayName("Subtraction")
    class Subtraction {

        @Test
        @DisplayName("Subtract correctly")
        void subtractCorrectly() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("30.50"), Currency.RUB);

            Money result = m1.subtract(m2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("69.50"));
        }

        @Test
        @DisplayName("Subtraction can result in negative")
        void subtractionCanResultInNegative() {
            Money m1 = Money.of(new BigDecimal("30.00"), Currency.EUR);
            Money m2 = Money.of(new BigDecimal("50.00"), Currency.EUR);

            Money result = m1.subtract(m2);

            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("-20.00"));
            assertThat(result.isNegative()).isTrue();
        }
    }

    @Nested
    @DisplayName("Queries")
    class Queries {

        @Test
        @DisplayName("isZero returns true for zero amount")
        void isZeroReturnsTrue() {
            Money zero = Money.zero(Currency.RUB);

            assertThat(zero.isZero()).isTrue();
            assertThat(zero.isPositive()).isFalse();
            assertThat(zero.isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive returns true for positive amount")
        void isPositiveReturnsTrue() {
            Money positive = Money.of(new BigDecimal("10.00"), Currency.EUR);

            assertThat(positive.isPositive()).isTrue();
            assertThat(positive.isZero()).isFalse();
            assertThat(positive.isNegative()).isFalse();
        }

        @Test
        @DisplayName("isNegative returns true for negative amount")
        void isNegativeReturnsTrue() {
            Money positive = Money.of(new BigDecimal("10.00"), Currency.USD);
            Money negative = positive.negate();

            assertThat(negative.isNegative()).isTrue();
            assertThat(negative.isZero()).isFalse();
            assertThat(negative.isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Comparison")
    class Comparison {

        @Test
        @DisplayName("compareTo returns correct ordering")
        void compareToReturnsCorrectOrdering() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("50.00"), Currency.RUB);
            Money m3 = Money.of(new BigDecimal("100.00"), Currency.RUB);

            assertThat(m1.compareTo(m2)).isGreaterThan(0);
            assertThat(m2.compareTo(m1)).isLessThan(0);
            assertThat(m1.compareTo(m3)).isEqualTo(0);
        }

        @Test
        @DisplayName("isGreaterThan and isLessThan work correctly")
        void comparisonMethodsWork() {
            Money larger = Money.of(new BigDecimal("100.00"), Currency.EUR);
            Money smaller = Money.of(new BigDecimal("50.00"), Currency.EUR);

            assertThat(larger.isGreaterThan(smaller)).isTrue();
            assertThat(smaller.isLessThan(larger)).isTrue();
            assertThat(larger.isLessThan(smaller)).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Equal amounts with same currency are equal")
        void equalAmountsAreEqual() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("100.00"), Currency.RUB);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("Different amounts are not equal")
        void differentAmountsNotEqual() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("100.01"), Currency.RUB);

            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("Same amount with different currency not equal")
        void differentCurrencyNotEqual() {
            Money m1 = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money m2 = Money.of(new BigDecimal("100.00"), Currency.EUR);

            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("Trailing zeros don't affect equality")
        void trailingZerosDontAffectEquality() {
            Money m1 = Money.of(new BigDecimal("100"), Currency.USD);
            Money m2 = Money.of(new BigDecimal("100.00"), Currency.USD);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("toString returns currency code and amount")
        void toStringReturnsCodeAndAmount() {
            Money money = Money.of(new BigDecimal("1500.50"), Currency.RUB);

            assertThat(money.toString()).isEqualTo("RUB 1500.50");
        }

        @Test
        @DisplayName("toFormattedString returns amount with symbol")
        void toFormattedStringReturnsWithSymbol() {
            Money money = Money.of(new BigDecimal("1500.50"), Currency.RUB);

            assertThat(money.toFormattedString()).contains("1500.50").contains("\u20BD");
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("Operations return new instances")
        void operationsReturnNewInstances() {
            Money original = Money.of(new BigDecimal("100.00"), Currency.RUB);
            Money toAdd = Money.of(new BigDecimal("50.00"), Currency.RUB);

            Money result = original.add(toAdd);

            assertThat(result).isNotSameAs(original);
            assertThat(original.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("negate returns new instance")
        void negateReturnsNewInstance() {
            Money original = Money.of(new BigDecimal("100.00"), Currency.EUR);

            Money negated = original.negate();

            assertThat(negated).isNotSameAs(original);
            assertThat(original.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(negated.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }
    }
}
