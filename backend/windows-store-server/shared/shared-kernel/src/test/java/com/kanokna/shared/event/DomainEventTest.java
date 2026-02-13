package com.kanokna.shared.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainEvent")
class DomainEventTest {

    private record SampleEvent(
        String eventId,
        Instant occurredAt,
        String aggregateId,
        String aggregateType,
        long version
    ) implements DomainEvent {
    }

    @Test
    @DisplayName("type returns fully qualified class name by default")
    void typeReturnsFullyQualifiedClassName() {
        DomainEvent event = new SampleEvent("evt-1", Instant.parse("2026-01-01T00:00:00Z"), "agg-1", "Order", 2L);

        assertThat(event.type()).isEqualTo(SampleEvent.class.getName());
    }

    @Test
    @DisplayName("shortType returns simple class name by default")
    void shortTypeReturnsSimpleClassName() {
        DomainEvent event = new SampleEvent("evt-1", Instant.parse("2026-01-01T00:00:00Z"), "agg-1", "Order", 2L);

        assertThat(event.shortType()).isEqualTo("SampleEvent");
    }

    @Test
    @DisplayName("toMetadata maps event fields with null tracing ids")
    void toMetadataMapsEventFields() {
        Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");
        DomainEvent event = new SampleEvent("evt-1", occurredAt, "agg-1", "Order", 2L);

        EventMetadata metadata = event.toMetadata();

        assertThat(metadata.eventId()).isEqualTo("evt-1");
        assertThat(metadata.occurredAt()).isEqualTo(occurredAt);
        assertThat(metadata.aggregateId()).isEqualTo("agg-1");
        assertThat(metadata.aggregateType()).isEqualTo("Order");
        assertThat(metadata.version()).isEqualTo(2L);
        assertThat(metadata.correlationId()).isNull();
        assertThat(metadata.causationId()).isNull();
    }

    @Test
    @DisplayName("toMetadata with tracing ids includes correlation and causation")
    void toMetadataWithTracingIdsIncludesIds() {
        DomainEvent event = new SampleEvent("evt-2", Instant.parse("2026-01-02T00:00:00Z"), "agg-2", "Quote", 3L);

        EventMetadata metadata = event.toMetadata("corr-1", "cause-1");

        assertThat(metadata.correlationId()).isEqualTo("corr-1");
        assertThat(metadata.causationId()).isEqualTo("cause-1");
    }
}
