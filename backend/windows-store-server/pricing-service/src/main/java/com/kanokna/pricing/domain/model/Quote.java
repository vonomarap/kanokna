package com.kanokna.pricing.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing a calculated price quote.
 * Immutable snapshot of pricing calculation with full audit trail.
 */
public final class Quote {
    private final QuoteId quoteId;
    private final String productTemplateId;
    private final Money basePrice;
    private final List<PremiumLine> optionPremiums;
    private final Money discount;
    private final Money subtotal;
    private final Money tax;
    private final Money total;
    private final Instant validUntil;
    private final List<PricingDecision> decisionTrace;

    private Quote(Builder builder) {
        this.quoteId = Objects.requireNonNull(builder.quoteId, "Quote ID cannot be null");
        this.productTemplateId = Objects.requireNonNull(builder.productTemplateId, "Product template ID cannot be null");
        this.basePrice = Objects.requireNonNull(builder.basePrice, "Base price cannot be null");
        this.optionPremiums = Collections.unmodifiableList(new ArrayList<>(builder.optionPremiums));
        this.discount = Objects.requireNonNull(builder.discount, "Discount cannot be null");
        this.subtotal = Objects.requireNonNull(builder.subtotal, "Subtotal cannot be null");
        this.tax = Objects.requireNonNull(builder.tax, "Tax cannot be null");
        this.total = Objects.requireNonNull(builder.total, "Total cannot be null");
        this.validUntil = Objects.requireNonNull(builder.validUntil, "Valid until cannot be null");
        this.decisionTrace = Collections.unmodifiableList(new ArrayList<>(builder.decisionTrace));
    }

    public static Builder builder() {
        return new Builder();
    }

    public QuoteId getQuoteId() {
        return quoteId;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public List<PremiumLine> getOptionPremiums() {
        return optionPremiums;
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getTax() {
        return tax;
    }

    public Money getTotal() {
        return total;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public List<PricingDecision> getDecisionTrace() {
        return decisionTrace;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(validUntil);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return quoteId.equals(quote.quoteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteId);
    }

    public static class Builder {
        private QuoteId quoteId;
        private String productTemplateId;
        private Money basePrice;
        private List<PremiumLine> optionPremiums = new ArrayList<>();
        private Money discount;
        private Money subtotal;
        private Money tax;
        private Money total;
        private Instant validUntil;
        private List<PricingDecision> decisionTrace = new ArrayList<>();

        public Builder quoteId(QuoteId quoteId) {
            this.quoteId = quoteId;
            return this;
        }

        public Builder productTemplateId(String productTemplateId) {
            this.productTemplateId = productTemplateId;
            return this;
        }

        public Builder basePrice(Money basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder optionPremiums(List<PremiumLine> optionPremiums) {
            this.optionPremiums = optionPremiums;
            return this;
        }

        public Builder addPremiumLine(PremiumLine premiumLine) {
            this.optionPremiums.add(premiumLine);
            return this;
        }

        public Builder discount(Money discount) {
            this.discount = discount;
            return this;
        }

        public Builder subtotal(Money subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder tax(Money tax) {
            this.tax = tax;
            return this;
        }

        public Builder total(Money total) {
            this.total = total;
            return this;
        }

        public Builder validUntil(Instant validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder decisionTrace(List<PricingDecision> decisionTrace) {
            this.decisionTrace = decisionTrace;
            return this;
        }

        public Builder addDecision(PricingDecision decision) {
            this.decisionTrace.add(decision);
            return this;
        }

        public Quote build() {
            return new Quote(this);
        }
    }
}
