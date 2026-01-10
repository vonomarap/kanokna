package com.kanokna.cart.domain.service;

import com.kanokna.cart.domain.model.ConfigurationSnapshot.SelectedOptionSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConfigurationHashServiceTest {
    @Test
    @DisplayName("TC-FUNC-CART-ADD-001: hash stable across option order")
    void hashStableAcrossOptionOrder() {
        ConfigurationHashService service = new ConfigurationHashService();
        List<SelectedOptionSnapshot> optionsA = List.of(
            new SelectedOptionSnapshot("B", "2"),
            new SelectedOptionSnapshot("A", "1")
        );
        List<SelectedOptionSnapshot> optionsB = List.of(
            new SelectedOptionSnapshot("A", "1"),
            new SelectedOptionSnapshot("B", "2")
        );

        String first = service.computeHash("T-9", 100, 120, optionsA);
        String second = service.computeHash("T-9", 100, 120, optionsB);

        assertEquals(first, second);
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-002: hash changes with dimensions")
    void hashChangesWithDimensions() {
        ConfigurationHashService service = new ConfigurationHashService();
        List<SelectedOptionSnapshot> options = List.of(
            new SelectedOptionSnapshot("A", "1")
        );

        String first = service.computeHash("T-9", 100, 120, options);
        String second = service.computeHash("T-9", 110, 120, options);

        assertNotEquals(first, second);
    }
}
