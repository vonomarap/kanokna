package com.kanokna.pricing.application.dto;

import java.math.BigDecimal;

/**
 * Command DTO for creating a price book.
 */
public class CreatePriceBookCommand {
    private String productTemplateId;
    private String currency;
    private BigDecimal pricePerM2;
    private BigDecimal minimumAreaM2;
    private BigDecimal minimumCharge;
    private String createdBy;

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

