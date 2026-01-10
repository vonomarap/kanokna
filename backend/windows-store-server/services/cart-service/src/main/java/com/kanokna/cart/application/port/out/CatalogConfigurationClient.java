package com.kanokna.cart.application.port.out;

import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import java.util.List;

/**
 * Outbound port for catalog configuration validation.
 */
public interface CatalogConfigurationClient {
    ConfigurationValidationResult validateConfiguration(ConfigurationValidationRequest request);

    record ConfigurationValidationRequest(
        String productTemplateId,
        DimensionsDto dimensions,
        List<SelectedOptionDto> selectedOptions
    ) {
    }

    record ConfigurationValidationResult(
        boolean available,
        boolean valid,
        List<String> errors,
        List<BomLineDto> resolvedBom
    ) {
    }
}
