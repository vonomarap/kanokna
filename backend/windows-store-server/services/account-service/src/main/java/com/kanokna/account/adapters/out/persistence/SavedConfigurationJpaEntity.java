package com.kanokna.account.adapters.out.persistence;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saved_configurations", schema = "accounts")
public class SavedConfigurationJpaEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "product_template_id", nullable = false)
    private UUID productTemplateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration_snapshot", nullable = false, columnDefinition = "jsonb")
    private String configurationSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quote_snapshot", columnDefinition = "jsonb")
    private String quoteSnapshot;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getProductTemplateId() {
        return productTemplateId;
    }

    public void setProductTemplateId(UUID productTemplateId) {
        this.productTemplateId = productTemplateId;
    }

    public String getConfigurationSnapshot() {
        return configurationSnapshot;
    }

    public void setConfigurationSnapshot(String configurationSnapshot) {
        this.configurationSnapshot = configurationSnapshot;
    }

    public String getQuoteSnapshot() {
        return quoteSnapshot;
    }

    public void setQuoteSnapshot(String quoteSnapshot) {
        this.quoteSnapshot = quoteSnapshot;
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
}