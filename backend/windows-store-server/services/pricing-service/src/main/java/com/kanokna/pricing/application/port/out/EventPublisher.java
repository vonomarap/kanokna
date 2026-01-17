package com.kanokna.pricing.application.port.out;

import com.kanokna.pricing.domain.event.QuoteCalculatedEvent;

/**
 * Outbound port for publishing pricing domain events.
 */
public interface EventPublisher {
    void publishQuoteCalculated(QuoteCalculatedEvent event);
}
