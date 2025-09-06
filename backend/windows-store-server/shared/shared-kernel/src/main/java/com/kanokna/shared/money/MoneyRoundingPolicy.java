package com.kanokna.shared.money;

import java.math.BigDecimal;

public interface MoneyRoundingPolicy {
    BigDecimal round(BigDecimal amount, Currency currency);

    static MoneyRoundingPolicy defaultPolicy() {
        return new DefaultMoneyRoundingPolicy();
    }
}
