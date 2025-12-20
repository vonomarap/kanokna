package com.kanokna.order_service.adapters.out.kafka;

import com.kanokna.order_service.application.port.out.NotificationPublisher;
import com.kanokna.shared.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisherKafka implements NotificationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisherKafka.class);
    private static final String TOPIC = "notification.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationPublisherKafka(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        kafkaTemplate.send(TOPIC, event.eventId(), event);
        logger.info("[KAFKA][notification] published event={} topic={}", event.type(), TOPIC);
    }
}
