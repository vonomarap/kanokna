package com.kanokna.shared.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("DefaultMoneyRoundingPolicy")
class DefaultMoneyRoundingPolicyTest {

    @Test
    @DisplayName("round uses HALF_UP at currency scale")
    void roundUsesHalfUpAtCurrencyScale() {
        MoneyRoundingPolicy policy = DefaultMoneyRoundingPolicy.INSTANCE;

        assertThat(policy.round(new BigDecimal("10.125"), Currency.RUB))
            .isEqualByComparingTo("10.13");
        assertThat(policy.round(new BigDecimal("10.124"), Currency.RUB))
            .isEqualByComparingTo("10.12");
    }

    @Test
    @DisplayName("round rejects null amount")
    void roundRejectsNullAmount() {
        MoneyRoundingPolicy policy = DefaultMoneyRoundingPolicy.INSTANCE;

        assertThatIllegalArgumentException()
            .isThrownBy(() -> policy.round(null, Currency.EUR))
            .withMessage("amount and currency are required");
    }

    @Test
    @DisplayName("round rejects null currency")
    void roundRejectsNullCurrency() {
        MoneyRoundingPolicy policy = DefaultMoneyRoundingPolicy.INSTANCE;

        assertThatIllegalArgumentException()
            .isThrownBy(() -> policy.round(new BigDecimal("10.00"), null))
            .withMessage("amount and currency are required");
    }
}
