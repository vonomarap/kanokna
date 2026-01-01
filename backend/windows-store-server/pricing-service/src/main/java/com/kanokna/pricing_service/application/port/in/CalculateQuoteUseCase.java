package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing_service.application.dto.QuoteResponse;

/**
 * Inbound port for calculating price quotes.
 */
public interface CalculateQuoteUseCase {
    QuoteResponse calculateQuote(CalculateQuoteCommand command);
}

