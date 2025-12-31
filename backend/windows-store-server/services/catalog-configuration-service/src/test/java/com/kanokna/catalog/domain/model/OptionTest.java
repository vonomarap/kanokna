package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Option entity.
 */
class OptionTest {

    @Test
    @DisplayName("Create option with valid data")
    void createOption_Succeeds() {
        // When
        Option option = Option.create("PVC", "PVC frame material", "FRAME-PVC");

        // Then
        assertNotNull(option.getId());
        assertEquals("PVC", option.getName());
        assertEquals("PVC frame material", option.getDescription());
        assertEquals("FRAME-PVC", option.getSkuCode());
        assertFalse(option.isDefaultSelected());
    }

    @Test
    @DisplayName("Update option details")
    void updateDetails_UpdatesFields() {
        // Given
        Option option = Option.create("PVC", "Old description", "OLD-SKU");

        // When
        option.updateDetails("Aluminum", "New description", "NEW-SKU");

        // Then
        assertEquals("Aluminum", option.getName());
        assertEquals("New description", option.getDescription());
        assertEquals("NEW-SKU", option.getSkuCode());
    }

    @Test
    @DisplayName("Set display order")
    void setDisplayOrder_UpdatesOrder() {
        // Given
        Option option = Option.create("PVC", "Description", "SKU");

        // When
        option.setDisplayOrder(5);

        // Then
        assertEquals(5, option.getDisplayOrder());
    }

    @Test
    @DisplayName("Set default selected")
    void setDefaultSelected_UpdatesFlag() {
        // Given
        Option option = Option.create("PVC", "Description", "SKU");

        // When
        option.setDefaultSelected(true);

        // Then
        assertTrue(option.isDefaultSelected());
    }
}
