package com.kanokna.catalog.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for ProductTemplate (confined to adapter layer).
 * Maps to catalog_configuration.product_templates table.
 */
@Entity
@Table(name = "product_templates", schema = "catalog_configuration")
public class ProductTemplateJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "product_family", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductFamilyJpa productFamily;

    @Column(name = "min_width_cm", nullable = false)
    private int minWidthCm;

    @Column(name = "max_width_cm", nullable = false)
    private int maxWidthCm;

    @Column(name = "min_height_cm", nullable = false)
    private int minHeightCm;

    @Column(name = "max_height_cm", nullable = false)
    private int maxHeightCm;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TemplateStatusJpa status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // JPA requires no-arg constructor
    protected ProductTemplateJpaEntity() {
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductFamilyJpa getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(ProductFamilyJpa productFamily) {
        this.productFamily = productFamily;
    }

    public int getMinWidthCm() {
        return minWidthCm;
    }

    public void setMinWidthCm(int minWidthCm) {
        this.minWidthCm = minWidthCm;
    }

    public int getMaxWidthCm() {
        return maxWidthCm;
    }

    public void setMaxWidthCm(int maxWidthCm) {
        this.maxWidthCm = maxWidthCm;
    }

    public int getMinHeightCm() {
        return minHeightCm;
    }

    public void setMinHeightCm(int minHeightCm) {
        this.minHeightCm = minHeightCm;
    }

    public int getMaxHeightCm() {
        return maxHeightCm;
    }

    public void setMaxHeightCm(int maxHeightCm) {
        this.maxHeightCm = maxHeightCm;
    }

    public TemplateStatusJpa getStatus() {
        return status;
    }

    public void setStatus(TemplateStatusJpa status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum ProductFamilyJpa {
        WINDOW, DOOR, SLIDING_DOOR, FRENCH_DOOR, CASEMENT_WINDOW, TILT_AND_TURN_WINDOW, FIXED_WINDOW, AWNING_WINDOW
    }

    public enum TemplateStatusJpa {
        DRAFT, ACTIVE, ARCHIVED
    }
}
