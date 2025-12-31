package com.kanokna.catalog.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Group of selectable options (e.g., Material, Glazing, Color).
 * Part of ProductTemplate aggregate.
 */
public class OptionGroup {

    private final UUID id;
    private String name;
    private int displayOrder;
    private boolean required;
    private boolean multiSelect;
    private final List<Option> options;

    public OptionGroup(UUID id, String name, int displayOrder, boolean required, boolean multiSelect) {
        this.id = Objects.requireNonNull(id, "OptionGroup id cannot be null");
        this.name = Objects.requireNonNull(name, "OptionGroup name cannot be null");
        this.displayOrder = displayOrder;
        this.required = required;
        this.multiSelect = multiSelect;
        this.options = new ArrayList<>();
    }

    public static OptionGroup create(String name, boolean required, boolean multiSelect) {
        return new OptionGroup(UUID.randomUUID(), name, 0, required, multiSelect);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    // Business methods
    public void addOption(Option option) {
        Objects.requireNonNull(option, "Option cannot be null");
        if (!options.contains(option)) {
            options.add(option);
        }
    }

    public void removeOption(UUID optionId) {
        options.removeIf(opt -> opt.getId().equals(optionId));
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Option findOptionById(UUID optionId) {
        return options.stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionGroup that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
