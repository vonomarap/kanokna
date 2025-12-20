package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.QuoteConfigurationCommand;
import com.kanokna.pricing_service.application.dto.QuoteResponse;

public interface QuotePort {

    QuoteResponse quoteConfiguration(QuoteConfigurationCommand command);

    default QuoteResponse quoteCart(QuoteConfigurationCommand command) {
        return quoteConfiguration(command);
    }
}
