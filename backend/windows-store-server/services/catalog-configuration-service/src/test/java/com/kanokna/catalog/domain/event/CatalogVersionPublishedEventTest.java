package com.kanokna.catalog.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CatalogVersionPublishedEvent.
 */
class CatalogVersionPublishedEventTest {

    @Test
    @DisplayName("Create event with valid data")
    void createEvent_Succeeds() {
        // Given
        UUID catalogVersionId = UUID.randomUUID();

        // When
        CatalogVersionPublishedEvent event = CatalogVersionPublishedEvent.create(
            catalogVersionId,
            1,
            5,
            "admin-user"
        );

        // Then
        assertNotNull(event.eventId());
        assertNotNull(event.occurredAt());
        assertEquals(catalogVersionId, event.catalogVersionId());
        assertEquals(1, event.versionNumber());
        assertEquals(5, event.templateCount());
        assertEquals("admin-user", event.publishedBy());
    }

    @Test
    @DisplayName("Version number must be positive")
    void zeroVersionNumber_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> CatalogVersionPublishedEvent.create(UUID.randomUUID(), 0, 5, "user"));
    }

    @Test
    @DisplayName("Template count cannot be negative")
    void negativeTemplateCount_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> CatalogVersionPublishedEvent.create(UUID.randomUUID(), 1, -1, "user"));
    }
}
