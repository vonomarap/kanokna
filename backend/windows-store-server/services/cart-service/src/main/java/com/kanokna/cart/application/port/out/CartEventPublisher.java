package com.kanokna.cart.application.port.out;

import com.kanokna.shared.event.DomainEvent;

/**
 * Outbound port for cart domain event publishing.
 */
public interface CartEventPublisher {
    void publish(DomainEvent event);
}
