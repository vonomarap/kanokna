package com.kanokna.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Single line in a bill of materials template.
 * Contains SKU, quantity formula, and optional condition.
 */
public class BomLine {

    private final UUID id;
    private String sku;
    private String description;
    private String quantityFormula;  // e.g., "1", "CEIL(width_cm / 100)", "2 * height_cm"
    private String conditionExpression;  // e.g., "hasOption('reinforced-profile')"

    public BomLine(UUID id, String sku, String description, String quantityFormula, String conditionExpression) {
        this.id = Objects.requireNonNull(id, "BomLine id cannot be null");
        this.sku = Objects.requireNonNull(sku, "sku cannot be null");
        this.description = description;
        this.quantityFormula = Objects.requireNonNull(quantityFormula, "quantityFormula cannot be null");
        this.conditionExpression = conditionExpression;
    }

    public static BomLine create(String sku, String description, String quantityFormula) {
        return new BomLine(UUID.randomUUID(), sku, description, quantityFormula, null);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public String getQuantityFormula() {
        return quantityFormula;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    // Business methods
    public void updateDetails(String sku, String description, String quantityFormula) {
        this.sku = Objects.requireNonNull(sku, "sku cannot be null");
        this.description = description;
        this.quantityFormula = Objects.requireNonNull(quantityFormula, "quantityFormula cannot be null");
    }

    public void setCondition(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BomLine bomLine)) return false;
        return Objects.equals(id, bomLine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
