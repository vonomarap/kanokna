package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "price_books", schema = "pricing")
public class PriceBookJpaEntity {
    @Id
    private UUID id;

    @Column(name = "product_template_id", nullable = false, length = 100)
    private String productTemplateId;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "price_per_m2", nullable = false)
    private BigDecimal pricePerM2;

    @Column(name = "minimum_area_m2", nullable = false)
    private BigDecimal minimumAreaM2;

    @Column(name = "minimum_charge")
    private BigDecimal minimumCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PriceBookStatus status;

    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "priceBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionPremiumJpaEntity> optionPremiums = new ArrayList<>();

    protected PriceBookJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public void setProductTemplateId(String productTemplateId) {
        this.productTemplateId = productTemplateId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPricePerM2() {
        return pricePerM2;
    }

    public void setPricePerM2(BigDecimal pricePerM2) {
        this.pricePerM2 = pricePerM2;
    }

    public BigDecimal getMinimumAreaM2() {
        return minimumAreaM2;
    }

    public void setMinimumAreaM2(BigDecimal minimumAreaM2) {
        this.minimumAreaM2 = minimumAreaM2;
    }

    public BigDecimal getMinimumCharge() {
        return minimumCharge;
    }

    public void setMinimumCharge(BigDecimal minimumCharge) {
        this.minimumCharge = minimumCharge;
    }

    public PriceBookStatus getStatus() {
        return status;
    }

    public void setStatus(PriceBookStatus status) {
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OptionPremiumJpaEntity> getOptionPremiums() {
        return optionPremiums;
    }

    public void setOptionPremiums(List<OptionPremiumJpaEntity> optionPremiums) {
        this.optionPremiums = optionPremiums;
    }
}
