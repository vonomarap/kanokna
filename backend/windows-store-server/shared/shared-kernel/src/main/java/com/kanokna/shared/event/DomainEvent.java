package com.kanokna.shared.event;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    Instant occurredAt();
    
    default String type() { 
        return this.getClass().getName(); 
    }
}
