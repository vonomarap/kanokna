package com.kanokna.shared.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("EventMetadata")
class EventMetadataTest {

    @Test
    @DisplayName("canonical constructor requires mandatory fields")
    void canonicalConstructorRequiresMandatoryFields() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertThatNullPointerException()
            .isThrownBy(() -> new EventMetadata(null, now, "agg-1", "Order", 1L, null, null))
            .withMessage("eventId cannot be null");

        assertThatNullPointerException()
            .isThrownBy(() -> new EventMetadata("evt-1", null, "agg-1", "Order", 1L, null, null))
            .withMessage("occurredAt cannot be null");

        assertThatNullPointerException()
            .isThrownBy(() -> new EventMetadata("evt-1", now, null, "Order", 1L, null, null))
            .withMessage("aggregateId cannot be null");

        assertThatNullPointerException()
            .isThrownBy(() -> new EventMetadata("evt-1", now, "agg-1", null, 1L, null, null))
            .withMessage("aggregateType cannot be null");
    }

    @Test
    @DisplayName("canonical constructor rejects negative version")
    void canonicalConstructorRejectsNegativeVersion() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> new EventMetadata("evt-1", now, "agg-1", "Order", -1L, null, null))
            .withMessage("version cannot be negative");
    }

    @Test
    @DisplayName("create generates event id and timestamp without tracing ids")
    void createGeneratesEventIdAndTimestamp() {
        EventMetadata metadata = EventMetadata.create("agg-10", "Order", 4L);

        assertThatCodeIsUuid(metadata.eventId());
        assertThat(metadata.occurredAt()).isNotNull();
        assertThat(metadata.aggregateId()).isEqualTo("agg-10");
        assertThat(metadata.aggregateType()).isEqualTo("Order");
        assertThat(metadata.version()).isEqualTo(4L);
        assertThat(metadata.correlationId()).isNull();
        assertThat(metadata.causationId()).isNull();
    }

    @Test
    @DisplayName("create with tracing ids stores provided values")
    void createWithTracingIdsStoresProvidedValues() {
        EventMetadata metadata = EventMetadata.create("agg-11", "Quote", 7L, "corr-7", "cause-7");

        assertThatCodeIsUuid(metadata.eventId());
        assertThat(metadata.correlationId()).isEqualTo("corr-7");
        assertThat(metadata.causationId()).isEqualTo("cause-7");
    }

    @Test
    @DisplayName("withCorrelationId returns updated copy and keeps other fields")
    void withCorrelationIdReturnsUpdatedCopy() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        EventMetadata original = new EventMetadata("evt-20", now, "agg-20", "Cart", 3L, null, "cause-20");

        EventMetadata updated = original.withCorrelationId("corr-20");

        assertThat(updated.correlationId()).isEqualTo("corr-20");
        assertThat(updated.causationId()).isEqualTo("cause-20");
        assertThat(updated.eventId()).isEqualTo("evt-20");
        assertThat(updated).isNotSameAs(original);
    }

    @Test
    @DisplayName("withCausationId returns updated copy and keeps other fields")
    void withCausationIdReturnsUpdatedCopy() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        EventMetadata original = new EventMetadata("evt-30", now, "agg-30", "Cart", 3L, "corr-30", null);

        EventMetadata updated = original.withCausationId("cause-30");

        assertThat(updated.causationId()).isEqualTo("cause-30");
        assertThat(updated.correlationId()).isEqualTo("corr-30");
        assertThat(updated.eventId()).isEqualTo("evt-30");
        assertThat(updated).isNotSameAs(original);
    }

    private static void assertThatCodeIsUuid(String value) {
        assertThat(UUID.fromString(value)).isNotNull();
    }
}
