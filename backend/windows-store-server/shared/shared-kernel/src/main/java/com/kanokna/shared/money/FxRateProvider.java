package com.kanokna.shared.money;

import java.util.Optional;

public interface FxRateProvider {
    Optional<FxRate> getRate(Currency from, Currency to);
}

