package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_rule")
public class TaxRuleJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String region;

    @Column(name = "product_type")
    private String productType;

    @Column(nullable = false, precision = 5, scale = 3)
    private BigDecimal rate;

    @Column(name = "rounding_policy", nullable = false)
    private String roundingPolicy;

    protected TaxRuleJpaEntity() {}

    public TaxRuleJpaEntity(String id, String region, String productType, BigDecimal rate, String roundingPolicy) {
        this.id = id;
        this.region = region;
        this.productType = productType;
        this.rate = rate;
        this.roundingPolicy = roundingPolicy;
    }

    public String getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getProductType() {
        return productType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getRoundingPolicy() {
        return roundingPolicy;
    }
}
