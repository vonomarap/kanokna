package com.kanokna.cart.application.port.out;

import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import java.util.List;

/**
 * Outbound port for catalog configuration validation.
 */
public interface CatalogConfigurationPort {
    ValidationResult validateConfiguration(ConfigurationSnapshot snapshot);

    record ValidationResult(
        boolean available,
        boolean valid,
        List<String> errors,
        List<ConfigurationSnapshot.BomLineSnapshot> resolvedBom
    ) {
        public static ValidationResult unavailable() {
            return new ValidationResult(false, false, List.of(), List.of());
        }
    }
}
