package com.kanokna.pricing_service.adapters.out.memory;

import com.kanokna.pricing_service.application.port.out.FxRatePort;
import com.kanokna.shared.money.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class PassthroughFxRatePort implements FxRatePort {
    @Override
    public Money convert(Money amount, Currency targetCurrency) {
        if (amount.getCurrency().equals(targetCurrency)) {
            return amount;
        }
        throw new UnsupportedOperationException("FX conversion not implemented in passthrough adapter");
    }
}
