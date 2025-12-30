package com.kanokna.shared.event;

import java.time.Instant;

/**
 * Base interface for all domain events in the system.
 * <p>
 * Domain events represent something significant that happened in the domain.
 * They are used for:
 * <ul>
 *   <li>Cross-service communication via message broker (Kafka)</li>
 *   <li>Event sourcing and audit trails</li>
 *   <li>Triggering side effects in other bounded contexts</li>
 * </ul>
 *
 * <h2>Implementation Guidelines:</h2>
 * <ul>
 *   <li>Events should be immutable (use records)</li>
 *   <li>Event names should be past tense (OrderPlaced, PaymentReceived)</li>
 *   <li>Include all data needed by consumers (no lazy loading)</li>
 *   <li>Use primitive/value types - no entity references</li>
 * </ul>
 *
 * <h2>Example Implementation:</h2>
 * <pre>{@code
 * public record OrderPlaced(
 *     String eventId,
 *     Instant occurredAt,
 *     String aggregateId,
 *     String orderId,
 *     String customerId,
 *     Money total
 * ) implements DomainEvent {
 *
 *     @Override
 *     public String aggregateType() {
 *         return "Order";
 *     }
 *
 *     @Override
 *     public long version() {
 *         return 1L;
 *     }
 * }
 * }</pre>
 *
 * @see EventMetadata
 */
public interface DomainEvent {

    /**
     * Returns the unique identifier of this event occurrence.
     * <p>
     * Should be a UUID or similar globally unique identifier.
     *
     * @return the event ID
     */
    String eventId();

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return the occurrence timestamp
     */
    Instant occurredAt();

    /**
     * Returns the identifier of the aggregate that emitted this event.
     * <p>
     * For entity-centric events, this is the entity's ID.
     *
     * @return the aggregate ID
     */
    String aggregateId();

    /**
     * Returns the type name of the aggregate.
     * <p>
     * Examples: "Order", "Configuration", "Account"
     *
     * @return the aggregate type name
     */
    String aggregateType();

    /**
     * Returns the version of the aggregate after this event.
     * <p>
     * Used for optimistic concurrency control in event sourcing.
     *
     * @return the aggregate version
     */
    long version();

    /**
     * Returns the fully-qualified event type name.
     * <p>
     * By default, uses the implementing class's name.
     *
     * @return the event type
     */
    default String type() {
        return this.getClass().getName();
    }

    /**
     * Returns a short event type name (simple class name).
     *
     * @return the simple event type name
     */
    default String shortType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Creates an EventMetadata from this event's properties.
     *
     * @return the event metadata
     */
    default EventMetadata toMetadata() {
        return new EventMetadata(
            eventId(),
            occurredAt(),
            aggregateId(),
            aggregateType(),
            version(),
            null,
            null
        );
    }

    /**
     * Creates an EventMetadata with correlation information.
     *
     * @param correlationId the correlation ID for distributed tracing
     * @param causationId   the ID of the event/command that caused this event
     * @return the event metadata with tracing info
     */
    default EventMetadata toMetadata(String correlationId, String causationId) {
        return new EventMetadata(
            eventId(),
            occurredAt(),
            aggregateId(),
            aggregateType(),
            version(),
            correlationId,
            causationId
        );
    }
}
