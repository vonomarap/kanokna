package com.kanokna.order_service.application.port.out;

import com.kanokna.shared.event.DomainEvent;

public interface OutboxPublisher {

    void publish(DomainEvent event);
}
