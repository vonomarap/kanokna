package com.kanokna.pricing_service.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Command DTO for creating a campaign.
 */
public class CreateCampaignCommand {
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Set<String> applicableProducts;
    private Instant startDate;
    private Instant endDate;
    private int priority;
    private String createdBy;

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

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public Set<String> getApplicableProducts() {
        return applicableProducts;
    }

    public void setApplicableProducts(Set<String> applicableProducts) {
        this.applicableProducts = applicableProducts;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

