package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.OutboxPublisher;
import com.kanokna.shared.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpOutboxPublisher implements OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NoOpOutboxPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        logger.debug("[OUTBOX][noop] type={} id={}", event.type(), event.eventId());
    }
}
