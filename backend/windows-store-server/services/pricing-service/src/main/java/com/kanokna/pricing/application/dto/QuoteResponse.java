package com.kanokna.pricing.application.dto;

import com.kanokna.pricing.domain.model.PremiumLine;
import com.kanokna.pricing.domain.model.PricingDecision;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO with calculated quote details.
 */
public class QuoteResponse {
    private String quoteId;
    private String productTemplateId;
    private String basePrice;
    private List<PremiumLineDto> optionPremiums;
    private String discount;
    private String subtotal;
    private String tax;
    private String total;
    private String currency;
    private Instant validUntil;
    private List<PricingDecisionDto> decisionTrace;

    public static class PremiumLineDto {
        private String optionId;
        private String optionName;
        private String amount;

        public static PremiumLineDto from(PremiumLine line) {
            PremiumLineDto dto = new PremiumLineDto();
            dto.optionId = line.getOptionId();
            dto.optionName = line.getOptionName();
            dto.amount = line.getAmount().toString();
            return dto;
        }

        public String getOptionId() { return optionId; }
        public String getOptionName() { return optionName; }
        public String getAmount() { return amount; }
    }

    public static class PricingDecisionDto {
        private String step;
        private String ruleApplied;
        private String result;

        public static PricingDecisionDto from(PricingDecision decision) {
            PricingDecisionDto dto = new PricingDecisionDto();
            dto.step = decision.getStep();
            dto.ruleApplied = decision.getRuleApplied();
            dto.result = decision.getResult();
            return dto;
        }

        public String getStep() { return step; }
        public String getRuleApplied() { return ruleApplied; }
        public String getResult() { return result; }
    }

    // Getters and setters
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }

    public String getProductTemplateId() { return productTemplateId; }
    public void setProductTemplateId(String productTemplateId) { this.productTemplateId = productTemplateId; }

    public String getBasePrice() { return basePrice; }
    public void setBasePrice(String basePrice) { this.basePrice = basePrice; }

    public List<PremiumLineDto> getOptionPremiums() { return optionPremiums; }
    public void setOptionPremiums(List<PremiumLineDto> optionPremiums) { this.optionPremiums = optionPremiums; }

    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getSubtotal() { return subtotal; }
    public void setSubtotal(String subtotal) { this.subtotal = subtotal; }

    public String getTax() { return tax; }
    public void setTax(String tax) { this.tax = tax; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }

    public List<PricingDecisionDto> getDecisionTrace() { return decisionTrace; }
    public void setDecisionTrace(List<PricingDecisionDto> decisionTrace) { this.decisionTrace = decisionTrace; }
}

