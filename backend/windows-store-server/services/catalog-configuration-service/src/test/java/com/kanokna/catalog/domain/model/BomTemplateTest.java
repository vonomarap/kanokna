package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BomTemplate aggregate.
 */
class BomTemplateTest {

    @Test
    @DisplayName("Create BOM template with product template ID")
    void createBomTemplate_Succeeds() {
        // Given
        ProductTemplateId productTemplateId = ProductTemplateId.generate();

        // When
        BomTemplate bomTemplate = BomTemplate.create(productTemplateId);

        // Then
        assertNotNull(bomTemplate.getId());
        assertEquals(productTemplateId, bomTemplate.getProductTemplateId());
        assertEquals(1, bomTemplate.getVersion());
        assertTrue(bomTemplate.getBomLines().isEmpty());
    }

    @Test
    @DisplayName("Add BOM line to template")
    void addLine_Succeeds() {
        // Given
        BomTemplate bomTemplate = BomTemplate.create(ProductTemplateId.generate());
        BomLine line = BomLine.create("FRAME-001", "Frame profile", "2");

        // When
        bomTemplate.addLine(line);

        // Then
        assertEquals(1, bomTemplate.getBomLines().size());
        assertEquals("FRAME-001", bomTemplate.getBomLines().get(0).getSku());
    }

    @Test
    @DisplayName("Remove BOM line from template")
    void removeLine_Succeeds() {
        // Given
        BomTemplate bomTemplate = BomTemplate.create(ProductTemplateId.generate());
        BomLine line = BomLine.create("FRAME-001", "Frame profile", "2");
        bomTemplate.addLine(line);

        // When
        bomTemplate.removeLine(line.getId());

        // Then
        assertTrue(bomTemplate.getBomLines().isEmpty());
    }

    @Test
    @DisplayName("Increment version increases version number")
    void incrementVersion_IncreasesVersion() {
        // Given
        BomTemplate bomTemplate = BomTemplate.create(ProductTemplateId.generate());
        int originalVersion = bomTemplate.getVersion();

        // When
        bomTemplate.incrementVersion();

        // Then
        assertEquals(originalVersion + 1, bomTemplate.getVersion());
    }
}
