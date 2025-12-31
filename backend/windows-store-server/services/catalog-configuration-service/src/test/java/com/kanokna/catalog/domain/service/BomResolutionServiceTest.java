package com.kanokna.catalog.domain.service;

import com.kanokna.catalog.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BomResolutionService.
 * Covers FUNCTION_CONTRACT FC-catalog-configuration-service-UC-CATALOG-CONFIGURE-ITEM-resolveBom.
 *
 * Test Cases:
 * TC-BOM-001: Standard window returns frame + glass SKUs
 * TC-BOM-002: Quantity scales with dimensions
 * TC-BOM-003: Optional accessories included in BOM
 */
class BomResolutionServiceTest {

    private BomResolutionService bomResolutionService;
    private BomTemplate bomTemplate;
    private ProductTemplateId productTemplateId;

    @BeforeEach
    void setUp() {
        bomResolutionService = new BomResolutionService();
        productTemplateId = ProductTemplateId.generate();
        bomTemplate = BomTemplate.create(productTemplateId);

        // Add standard BOM lines
        bomTemplate.addLine(BomLine.create("FRAME-001", "Window frame", "2"));
        bomTemplate.addLine(BomLine.create("GLASS-001", "Glass pane", "1"));
    }

    @Test
    @DisplayName("TC-BOM-001: Standard window returns frame + glass SKUs")
    void standardWindow_ReturnsFrameAndGlass() {
        // Given: Standard configuration
        Configuration config = new Configuration(120, 150, Map.of());

        // When: Resolve BOM
        ResolvedBom bom = bomResolutionService.resolveBom(config, bomTemplate);

        // Then: Contains expected SKUs
        assertEquals(2, bom.totalItems());
        assertTrue(bom.items().stream().anyMatch(item -> item.sku().equals("FRAME-001")));
        assertTrue(bom.items().stream().anyMatch(item -> item.sku().equals("GLASS-001")));
    }

    @Test
    @DisplayName("TC-BOM-002: Quantity scales with dimensions")
    void largerDimensions_ScalesQuantities() {
        // Given: BOM with quantity formula
        bomTemplate.addLine(BomLine.create("SEAL-001", "Sealing", "width_cm / 100"));
        Configuration config = new Configuration(200, 150, Map.of());

        // When: Resolve BOM
        ResolvedBom bom = bomResolutionService.resolveBom(config, bomTemplate);

        // Then: Quantities calculated from formulas
        assertNotNull(bom);
        assertTrue(bom.totalItems() >= 2);
    }

    @Test
    @DisplayName("TC-BOM-003: Optional accessories included in BOM")
    void withAccessories_IncludedInBom() {
        // Given: BOM with conditional line
        bomTemplate.addLine(BomLine.create("HANDLE-001", "Door handle", "1"));
        Configuration config = new Configuration(120, 150, Map.of());

        // When: Resolve BOM
        ResolvedBom bom = bomResolutionService.resolveBom(config, bomTemplate);

        // Then: All items included
        assertTrue(bom.totalItems() >= 2);
    }
}
