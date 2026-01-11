package com.kanokna.catalog.adapters.out.kafka;

import com.kanokna.catalog.application.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event publisher adapter.
 * Publishes domain events to Kafka topics.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public <T> void publish(String topic, T event) {
        log.debug("Publishing event to topic {}: {}", topic, event.getClass().getSimpleName());
        try {
            kafkaTemplate.send(topic, event);
            log.info("Event published successfully to topic {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}", topic, e);
            throw new RuntimeException("ERR-EVENT-PUBLISH-FAILED: " + e.getMessage(), e);
        }
    }
}
