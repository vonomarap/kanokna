package com.kanokna.catalog.domain.model;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing a resolved bill of materials for a configuration.
 * Contains concrete SKUs and quantities.
 */
public record ResolvedBom(
    ProductTemplateId productTemplateId,
    List<BomItem> items
) {

    public ResolvedBom {
        Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        Objects.requireNonNull(items, "items cannot be null");
        items = List.copyOf(items);
    }

    public record BomItem(
        String sku,
        String description,
        int quantity
    ) {
        public BomItem {
            Objects.requireNonNull(sku, "sku cannot be null");
            if (quantity <= 0) {
                throw CatalogDomainErrors.invalidQuantity(quantity);
            }
        }
    }

    public int totalItems() {
        return items.size();
    }
}
