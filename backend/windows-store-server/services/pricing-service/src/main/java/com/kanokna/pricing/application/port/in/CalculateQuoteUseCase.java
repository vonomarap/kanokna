package com.kanokna.pricing.application.port.in;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.application.dto.QuoteResponse;

/**
 * Inbound port for calculating price quotes.
 */
public interface CalculateQuoteUseCase {
    QuoteResponse calculateQuote(CalculateQuoteCommand command);
}

