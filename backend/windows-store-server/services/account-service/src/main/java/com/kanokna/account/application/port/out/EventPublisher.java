package com.kanokna.account.application.port.out;

/**
 * Outbound port for domain event publishing.
 */
public interface EventPublisher {
    <T> void publish(String topic, T event);
}