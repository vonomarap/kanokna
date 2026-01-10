package com.kanokna.cart.application.port.out;

/**
 * Outbound port for cart domain event publishing.
 */
public interface EventPublisher {
    <T> void publish(String topic, T event);
}
