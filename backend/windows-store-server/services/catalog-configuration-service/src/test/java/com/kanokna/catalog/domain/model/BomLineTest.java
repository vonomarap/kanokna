package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BomLine entity.
 */
class BomLineTest {

    @Test
    @DisplayName("Create BOM line with valid data")
    void createBomLine_Succeeds() {
        // When
        BomLine line = BomLine.create("FRAME-001", "Frame profile", "2");

        // Then
        assertNotNull(line.getId());
        assertEquals("FRAME-001", line.getSku());
        assertEquals("Frame profile", line.getDescription());
        assertEquals("2", line.getQuantityFormula());
        assertNull(line.getConditionExpression());
    }

    @Test
    @DisplayName("Update BOM line details")
    void updateDetails_UpdatesFields() {
        // Given
        BomLine line = BomLine.create("OLD-SKU", "Old description", "1");

        // When
        line.updateDetails("NEW-SKU", "New description", "width_cm / 100");

        // Then
        assertEquals("NEW-SKU", line.getSku());
        assertEquals("New description", line.getDescription());
        assertEquals("width_cm / 100", line.getQuantityFormula());
    }

    @Test
    @DisplayName("Set condition expression")
    void setCondition_UpdatesCondition() {
        // Given
        BomLine line = BomLine.create("SKU", "Description", "1");

        // When
        line.setCondition("hasOption('reinforced')");

        // Then
        assertEquals("hasOption('reinforced')", line.getConditionExpression());
    }
}
