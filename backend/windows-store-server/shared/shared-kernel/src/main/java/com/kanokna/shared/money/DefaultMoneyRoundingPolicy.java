package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

final class DefaultMoneyRoundingPolicy implements MoneyRoundingPolicy {

    static final MoneyRoundingPolicy  INSTANCE = new DefaultMoneyRoundingPolicy();

    private DefaultMoneyRoundingPolicy() {

    }

    @Override
    public BigDecimal round(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null)
          throw new IllegalArgumentException("amount/currency required");

        int scale = currency.getDefaultFractionDigits();

        return amount.setScale(scale, RoundingMode.HALF_UP);
    }
}

