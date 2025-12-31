package com.kanokna.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OptionGroup entity.
 */
class OptionGroupTest {

    @Test
    @DisplayName("Create option group with valid data")
    void createOptionGroup_Succeeds() {
        // When
        OptionGroup optionGroup = OptionGroup.create("Material", true, false);

        // Then
        assertEquals("Material", optionGroup.getName());
        assertTrue(optionGroup.isRequired());
        assertFalse(optionGroup.isMultiSelect());
        assertTrue(optionGroup.getOptions().isEmpty());
    }

    @Test
    @DisplayName("Add option to group")
    void addOption_Succeeds() {
        // Given
        OptionGroup optionGroup = OptionGroup.create("Material", true, false);
        Option option = Option.create("PVC", "PVC frame", "FRAME-PVC");

        // When
        optionGroup.addOption(option);

        // Then
        assertEquals(1, optionGroup.getOptions().size());
        assertEquals("PVC", optionGroup.getOptions().get(0).getName());
    }

    @Test
    @DisplayName("Remove option from group")
    void removeOption_Succeeds() {
        // Given
        OptionGroup optionGroup = OptionGroup.create("Material", true, false);
        Option option = Option.create("PVC", "PVC frame", "FRAME-PVC");
        optionGroup.addOption(option);

        // When
        optionGroup.removeOption(option.getId());

        // Then
        assertTrue(optionGroup.getOptions().isEmpty());
    }

    @Test
    @DisplayName("Find option by ID")
    void findOptionById_ReturnsOption() {
        // Given
        OptionGroup optionGroup = OptionGroup.create("Material", true, false);
        Option option = Option.create("PVC", "PVC frame", "FRAME-PVC");
        optionGroup.addOption(option);

        // When
        Option found = optionGroup.findOptionById(option.getId());

        // Then
        assertNotNull(found);
        assertEquals("PVC", found.getName());
    }
}
