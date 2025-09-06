package com.kanokna.shared.money;

import java.math.BigDecimal;

public final class MoneyFx {
    private MoneyFx() {}

    public static Money convert(Money source, Currency target, FxRateProvider provider, MoneyRoundingPolicy policy) {
        if(source == null || target == null || provider == null || policy == null)
            throw new IllegalArgumentException("src or target or provider or policy required");
        
        if (source.currency() == target)
            return source;

        var rateOption = provider.getRate(source.currency(), target);
        if (rateOption.isEmpty())  
            throw new IllegalStateException("Fx rate not available for %s -> %s".formatted(source.currency(), target));
        var rate = rateOption.get().rate();

        BigDecimal raw = source.amount().multiply(rate);
        return Money.ofRounded(raw, target, policy);
    }
}
