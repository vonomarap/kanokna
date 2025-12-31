package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductTemplate aggregate.
 */
class ProductTemplateTest {

    @Test
    @DisplayName("New product template starts in DRAFT status")
    void newTemplate_IsDraft() {
        // When
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        // Then
        assertTrue(template.isDraft());
        assertFalse(template.isActive());
        assertFalse(template.isArchived());
        assertEquals(TemplateStatus.DRAFT, template.getStatus());
        assertEquals(0, template.getVersion());
    }

    @Test
    @DisplayName("Publishing DRAFT template makes it ACTIVE and increments version")
    void publishDraft_BecomesActive() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Test Window",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        // When
        template.publish();

        // Then
        assertTrue(template.isActive());
        assertEquals(TemplateStatus.ACTIVE, template.getStatus());
        assertEquals(1, template.getVersion());
    }

    @Test
    @DisplayName("Publishing ACTIVE template throws exception")
    void publishActive_ThrowsException() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Test",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
        template.publish();

        // When/Then
        assertThrows(IllegalStateException.class, template::publish);
    }

    @Test
    @DisplayName("Archiving template sets ARCHIVED status")
    void archive_SetsArchivedStatus() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Test",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );

        // When
        template.archive();

        // Then
        assertTrue(template.isArchived());
        assertEquals(TemplateStatus.ARCHIVED, template.getStatus());
    }

    @Test
    @DisplayName("Updating ARCHIVED template throws exception")
    void updateArchived_ThrowsException() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Test",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
        template.archive();

        // When/Then
        assertThrows(IllegalStateException.class,
            () -> template.updateDetails("New Name", "New Description", DimensionConstraints.standard()));
    }

    @Test
    @DisplayName("Adding option group to template succeeds")
    void addOptionGroup_Succeeds() {
        // Given
        ProductTemplate template = ProductTemplate.create(
            "Test",
            "Description",
            ProductFamily.WINDOW,
            DimensionConstraints.standard()
        );
        OptionGroup optionGroup = OptionGroup.create("Material", true, false);

        // When
        template.addOptionGroup(optionGroup);

        // Then
        assertEquals(1, template.getOptionGroups().size());
        assertEquals("Material", template.getOptionGroups().get(0).getName());
    }
}
