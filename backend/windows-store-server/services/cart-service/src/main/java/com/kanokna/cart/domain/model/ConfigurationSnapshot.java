package com.kanokna.cart.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a configuration at the time of adding to cart.
 */
public record ConfigurationSnapshot(
    String productTemplateId,
    int widthCm,
    int heightCm,
    List<SelectedOptionSnapshot> selectedOptions,
    List<BomLineSnapshot> resolvedBom
) {
    public ConfigurationSnapshot {
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
        resolvedBom = resolvedBom == null ? List.of() : List.copyOf(resolvedBom);
    }

    public record SelectedOptionSnapshot(String optionGroupId, String optionId) {
        public SelectedOptionSnapshot {
            Objects.requireNonNull(optionGroupId, "optionGroupId cannot be null");
            Objects.requireNonNull(optionId, "optionId cannot be null");
        }
    }

    public record BomLineSnapshot(String sku, String description, int quantity) {
        public BomLineSnapshot {
            Objects.requireNonNull(sku, "sku cannot be null");
            Objects.requireNonNull(description, "description cannot be null");
        }
    }
}
