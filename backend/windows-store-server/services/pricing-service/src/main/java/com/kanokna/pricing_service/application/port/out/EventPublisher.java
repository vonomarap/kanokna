package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.event.QuoteCalculatedEvent;

/**
 * Outbound port for publishing pricing domain events.
 */
public interface EventPublisher {
    void publishQuoteCalculated(QuoteCalculatedEvent event);
}
