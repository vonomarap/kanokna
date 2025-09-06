package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

final class DefaultMoneyRoundingPolicy implements MoneyRoundingPolicy {
    private static final Map<Currency, Integer> SCALE = new EnumMap<>(Currency.class);
    static {
        SCALE.put(Currency.RUB, 0);
        SCALE.put(Currency.USD, 2);
        SCALE.put(Currency.EUR, 2);
        SCALE.put(Currency.KZT, 0);
    }

    @Override
    public BigDecimal round(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) throw new IllegalArgumentException("amount/currency required");
        int scale = SCALE.getOrDefault(currency, 2);
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }
}

