package com.kanokna.catalog.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: Bill of materials template for a product.
 * Defines SKUs and quantity formulas needed for a configuration.
 */
public class BomTemplate {

    private final UUID id;
    private final ProductTemplateId productTemplateId;
    private int version;
    private final List<BomLine> bomLines;

    public BomTemplate(UUID id, ProductTemplateId productTemplateId, int version) {
        this.id = Objects.requireNonNull(id, "BomTemplate id cannot be null");
        this.productTemplateId = Objects.requireNonNull(productTemplateId, "productTemplateId cannot be null");
        this.version = version;
        this.bomLines = new ArrayList<>();
    }

    public static BomTemplate create(ProductTemplateId productTemplateId) {
        return new BomTemplate(UUID.randomUUID(), productTemplateId, 1);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public ProductTemplateId getProductTemplateId() {
        return productTemplateId;
    }

    public int getVersion() {
        return version;
    }

    public List<BomLine> getBomLines() {
        return Collections.unmodifiableList(bomLines);
    }

    // Business methods
    public void addLine(BomLine bomLine) {
        Objects.requireNonNull(bomLine, "BomLine cannot be null");
        if (!bomLines.contains(bomLine)) {
            bomLines.add(bomLine);
        }
    }

    public void removeLine(UUID bomLineId) {
        bomLines.removeIf(line -> line.getId().equals(bomLineId));
    }

    public void incrementVersion() {
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BomTemplate that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
