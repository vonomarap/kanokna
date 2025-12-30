package com.kanokna.shared.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Metadata associated with a domain event.
 * <p>
 * This record captures standard event metadata required for event sourcing,
 * auditing, and message routing.
 *
 * @param eventId       Unique identifier for this event occurrence
 * @param occurredAt    Timestamp when the event occurred
 * @param aggregateId   ID of the aggregate that emitted the event
 * @param aggregateType Type name of the aggregate (e.g., "Order", "Configuration")
 * @param version       Version of the aggregate after the event (for optimistic locking)
 * @param correlationId Optional correlation ID for request tracing
 * @param causationId   Optional ID of the event/command that caused this event
 * @see DomainEvent
 */
public record EventMetadata(
    String eventId,
    Instant occurredAt,
    String aggregateId,
    String aggregateType,
    long version,
    String correlationId,
    String causationId
) {

    /**
     * Canonical constructor with validation.
     */
    public EventMetadata {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(aggregateType, "aggregateType cannot be null");
        if (version < 0) {
            throw new IllegalArgumentException("version cannot be negative");
        }
        // correlationId and causationId can be null
    }

    /**
     * Creates metadata with auto-generated eventId and current timestamp.
     *
     * @param aggregateId   the aggregate ID
     * @param aggregateType the aggregate type
     * @param version       the aggregate version
     * @return new EventMetadata instance
     */
    public static EventMetadata create(String aggregateId, String aggregateType, long version) {
        return new EventMetadata(
            UUID.randomUUID().toString(),
            Instant.now(),
            aggregateId,
            aggregateType,
            version,
            null,
            null
        );
    }

    /**
     * Creates metadata with tracing information.
     *
     * @param aggregateId   the aggregate ID
     * @param aggregateType the aggregate type
     * @param version       the aggregate version
     * @param correlationId the correlation ID for tracing
     * @param causationId   the ID of the causing event/command
     * @return new EventMetadata instance
     */
    public static EventMetadata create(
        String aggregateId,
        String aggregateType,
        long version,
        String correlationId,
        String causationId
    ) {
        return new EventMetadata(
            UUID.randomUUID().toString(),
            Instant.now(),
            aggregateId,
            aggregateType,
            version,
            correlationId,
            causationId
        );
    }

    /**
     * Creates a copy with updated correlation ID.
     *
     * @param correlationId the new correlation ID
     * @return new EventMetadata with updated correlation ID
     */
    public EventMetadata withCorrelationId(String correlationId) {
        return new EventMetadata(
            eventId,
            occurredAt,
            aggregateId,
            aggregateType,
            version,
            correlationId,
            causationId
        );
    }

    /**
     * Creates a copy with updated causation ID.
     *
     * @param causationId the new causation ID
     * @return new EventMetadata with updated causation ID
     */
    public EventMetadata withCausationId(String causationId) {
        return new EventMetadata(
            eventId,
            occurredAt,
            aggregateId,
            aggregateType,
            version,
            correlationId,
            causationId
        );
    }
}
