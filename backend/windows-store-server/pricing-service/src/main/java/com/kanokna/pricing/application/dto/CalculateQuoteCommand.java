package com.kanokna.pricing.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command DTO for calculating a price quote.
 */
public class CalculateQuoteCommand {
    private String productTemplateId;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private List<String> selectedOptionIds;
    private String currency;
    private String promoCode;
    private String region;

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public void setProductTemplateId(String productTemplateId) {
        this.productTemplateId = productTemplateId;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public List<String> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(List<String> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getRegion() {
        return region != null ? region : "RU";
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
