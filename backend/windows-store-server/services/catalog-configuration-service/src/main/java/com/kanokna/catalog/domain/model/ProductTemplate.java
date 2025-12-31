package com.kanokna.catalog.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: Product template defining a configurable window or door type.
 * Contains option groups, dimension constraints, and lifecycle status.
 */
public class ProductTemplate {

    private final ProductTemplateId id;
    private String name;
    private String description;
    private ProductFamily productFamily;
    private DimensionConstraints dimensionConstraints;
    private TemplateStatus status;
    private long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<OptionGroup> optionGroups;

    public ProductTemplate(
        ProductTemplateId id,
        String name,
        String description,
        ProductFamily productFamily,
        DimensionConstraints dimensionConstraints
    ) {
        this.id = Objects.requireNonNull(id, "ProductTemplateId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description;
        this.productFamily = Objects.requireNonNull(productFamily, "productFamily cannot be null");
        this.dimensionConstraints = Objects.requireNonNull(dimensionConstraints, "dimensionConstraints cannot be null");
        this.status = TemplateStatus.DRAFT;
        this.version = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.optionGroups = new ArrayList<>();
    }

    public static ProductTemplate create(
        String name,
        String description,
        ProductFamily productFamily,
        DimensionConstraints dimensionConstraints
    ) {
        return new ProductTemplate(
            ProductTemplateId.generate(),
            name,
            description,
            productFamily,
            dimensionConstraints
        );
    }

    // Getters
    public ProductTemplateId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductFamily getProductFamily() {
        return productFamily;
    }

    public DimensionConstraints getDimensionConstraints() {
        return dimensionConstraints;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OptionGroup> getOptionGroups() {
        return Collections.unmodifiableList(optionGroups);
    }

    // Business methods
    public void updateDetails(String name, String description, DimensionConstraints dimensionConstraints) {
        if (status == TemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot update archived template");
        }
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description;
        this.dimensionConstraints = Objects.requireNonNull(dimensionConstraints, "dimensionConstraints cannot be null");
        this.updatedAt = Instant.now();
    }

    public void addOptionGroup(OptionGroup optionGroup) {
        Objects.requireNonNull(optionGroup, "OptionGroup cannot be null");
        if (status == TemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot modify archived template");
        }
        if (!optionGroups.contains(optionGroup)) {
            optionGroups.add(optionGroup);
            this.updatedAt = Instant.now();
        }
    }

    public void removeOptionGroup(UUID optionGroupId) {
        if (status == TemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot modify archived template");
        }
        optionGroups.removeIf(og -> og.getId().equals(optionGroupId));
        this.updatedAt = Instant.now();
    }

    public void publish() {
        if (status == TemplateStatus.ACTIVE) {
            throw new IllegalStateException("Template is already active");
        }
        if (status == TemplateStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot publish archived template");
        }
        this.status = TemplateStatus.ACTIVE;
        this.version++;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public boolean isDraft() {
        return status == TemplateStatus.DRAFT;
    }

    public boolean isActive() {
        return status == TemplateStatus.ACTIVE;
    }

    public boolean isArchived() {
        return status == TemplateStatus.ARCHIVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductTemplate that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
