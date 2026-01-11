package com.kanokna.catalog.application.port.out;

/**
 * Outbound port: Domain event publishing (Kafka).
 */
public interface EventPublisher {

    <T> void publish(String topic, T event);
}
