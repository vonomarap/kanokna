package com.kanokna.pricing_service.application.port.out;

import com.kanokna.shared.money.Money;

import java.util.Currency;

public interface FxRatePort {

    Money convert(Money amount, Currency targetCurrency);
}
