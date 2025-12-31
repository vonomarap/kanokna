package com.kanokna.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Single selectable option within an OptionGroup.
 * Part of ProductTemplate aggregate.
 */
public class Option {

    private final UUID id;
    private String name;
    private String description;
    private String skuCode;
    private int displayOrder;
    private boolean defaultSelected;

    public Option(UUID id, String name, String description, String skuCode, int displayOrder, boolean defaultSelected) {
        this.id = Objects.requireNonNull(id, "Option id cannot be null");
        this.name = Objects.requireNonNull(name, "Option name cannot be null");
        this.description = description;
        this.skuCode = skuCode;
        this.displayOrder = displayOrder;
        this.defaultSelected = defaultSelected;
    }

    public static Option create(String name, String description, String skuCode) {
        return new Option(UUID.randomUUID(), name, description, skuCode, 0, false);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    // Business methods
    public void updateDetails(String name, String description, String skuCode) {
        this.name = Objects.requireNonNull(name, "Option name cannot be null");
        this.description = description;
        this.skuCode = skuCode;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option option)) return false;
        return Objects.equals(id, option.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
