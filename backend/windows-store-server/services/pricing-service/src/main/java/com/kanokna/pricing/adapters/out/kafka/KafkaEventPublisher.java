package com.kanokna.pricing.adapters.out.kafka;

import org.springframework.stereotype.Component;
import com.kanokna.pricing.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing.application.port.out.EventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka producer for pricing events.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {
    private static final String QUOTE_CALCULATED_TOPIC = "pricing.quote.calculated";

    private final KafkaTemplate<String, com.kanokna.pricing.v1.QuoteCalculatedEvent> kafkaTemplate;
    private final EventSerializer serializer;

    public KafkaEventPublisher(KafkaTemplate<String, com.kanokna.pricing.v1.QuoteCalculatedEvent> kafkaTemplate, EventSerializer serializer) {
        this.kafkaTemplate = kafkaTemplate;
        this.serializer = serializer;
    }

    @Override
    public void publishQuoteCalculated(QuoteCalculatedEvent event) {
        com.kanokna.pricing.v1.QuoteCalculatedEvent protoEvent = serializer.toQuoteCalculatedEvent(event);
        kafkaTemplate.send(QUOTE_CALCULATED_TOPIC, event.getQuoteId().toString(), protoEvent);
    }
}
