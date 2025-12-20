package com.kanokna.pricing_service.adapters.out.kafka;

import com.kanokna.pricing_service.application.port.out.OutboxPublisher;
import com.kanokna.shared.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOutboxPublisher implements OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOutboxPublisher.class);
    private static final String TOPIC = "pricing.updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaOutboxPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        kafkaTemplate.send(TOPIC, event.eventId(), event);
        logger.info("[KAFKA][pricing] published event={} topic={}", event.type(), TOPIC);
    }
}
