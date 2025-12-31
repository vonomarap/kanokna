package com.kanokna.catalog.domain.event;

import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductTemplatePublishedEvent.
 */
class ProductTemplatePublishedEventTest {

    @Test
    @DisplayName("Create event with valid data")
    void createEvent_Succeeds() {
        // Given
        ProductTemplateId productTemplateId = ProductTemplateId.generate();

        // When
        ProductTemplatePublishedEvent event = ProductTemplatePublishedEvent.create(
            productTemplateId,
            "Test Window",
            ProductFamily.WINDOW,
            1
        );

        // Then
        assertNotNull(event.eventId());
        assertNotNull(event.occurredAt());
        assertEquals(productTemplateId, event.productTemplateId());
        assertEquals("Test Window", event.productName());
        assertEquals(ProductFamily.WINDOW, event.productFamily());
        assertEquals(1, event.version());
    }
}
