package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.util.Currency;

public interface MoneyRoundingPolicy {
    BigDecimal round(BigDecimal amount, Currency currency);

    static MoneyRoundingPolicy defaultPolicy() {
      return DefaultMoneyRoundingPolicy.INSTANCE;
    }
}
