package com.kanokna.pricing_service.application.dto;

import java.math.BigDecimal;

/**
 * Command DTO for validating a promo code.
 */
public class ValidatePromoCodeCommand {
    private String promoCode;
    private BigDecimal subtotal;
    private String currency;

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

