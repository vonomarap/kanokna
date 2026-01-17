package com.kanokna.catalog.domain.model;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a customer's product configuration.
 * Immutable snapshot of dimension + selected options.
 */
public record Configuration(
    int widthCm,
    int heightCm,
    Map<String, UUID> selectedOptions  // optionGroupName -> optionId
) {

    public Configuration {
        Objects.requireNonNull(selectedOptions, "selectedOptions cannot be null");
        selectedOptions = Map.copyOf(selectedOptions); // defensive copy

        if (widthCm <= 0) {
            throw CatalogDomainErrors.invalidWidth(widthCm);
        }
        if (heightCm <= 0) {
            throw CatalogDomainErrors.invalidHeight(heightCm);
        }
    }

    public double getAreaM2() {
        return (widthCm / 100.0) * (heightCm / 100.0);
    }

    public boolean hasOption(UUID optionId) {
        return selectedOptions.containsValue(optionId);
    }

    public UUID getOptionForGroup(String optionGroupName) {
        return selectedOptions.get(optionGroupName);
    }
}
