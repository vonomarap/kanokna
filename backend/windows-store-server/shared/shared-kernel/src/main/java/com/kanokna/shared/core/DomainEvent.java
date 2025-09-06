package com.kanokna.shared.core;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    Instant occurredAt();
    
    default String type() { 
        return this.getClass().getName(); 
    }
}
