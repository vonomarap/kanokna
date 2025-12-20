package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.measure.DimensionsCm;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record ConfigurationSelection(
    Id templateId,
    Id tenantId,
    DimensionsCm dimensions,
    Map<String, String> optionSelections,
    String locale,
    long catalogVersion
) {
    public ConfigurationSelection {
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(optionSelections, "optionSelections");
        if (locale == null || locale.isBlank()) {
            throw new IllegalArgumentException("locale must be provided");
        }
        Map<String, String> normalized = new TreeMap<>();
        optionSelections.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                normalized.put(key.trim(), value.trim());
            }
        });
        optionSelections = Collections.unmodifiableMap(normalized);
    }

    public Optional<String> selectedValue(String attributeCode) {
        return Optional.ofNullable(optionSelections.get(attributeCode));
    }

    public boolean hasSelection(String attributeCode) {
        return optionSelections.containsKey(attributeCode);
    }

    /**
     * Deterministic signature for caching/idempotency at adapters.
     */
    public String signatureKey() {
        StringBuilder builder = new StringBuilder()
            .append(templateId.value())
            .append("|ver=").append(catalogVersion)
            .append("|dim=").append(dimensions.width()).append("x").append(dimensions.height())
            .append("|locale=").append(locale);

        optionSelections.forEach((key, value) -> builder.append("|").append(key).append("=").append(value));
        return builder.toString();
    }
}
