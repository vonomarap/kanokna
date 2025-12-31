package com.kanokna.pricing.domain.event;

import com.kanokna.pricing.domain.model.QuoteId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a price quote is calculated.
 * Used for analytics and audit trail.
 */
public class QuoteCalculatedEvent {
    private final QuoteId quoteId;
    private final String productTemplateId;
    private final String currency;
    private final String totalAmount;
    private final Instant calculatedAt;

    private QuoteCalculatedEvent(QuoteId quoteId, String productTemplateId,
                                String currency, String totalAmount, Instant calculatedAt) {
        this.quoteId = Objects.requireNonNull(quoteId);
        this.productTemplateId = Objects.requireNonNull(productTemplateId);
        this.currency = Objects.requireNonNull(currency);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.calculatedAt = Objects.requireNonNull(calculatedAt);
    }

    public static QuoteCalculatedEvent of(QuoteId quoteId, String productTemplateId,
                                         String currency, String totalAmount) {
        return new QuoteCalculatedEvent(quoteId, productTemplateId, currency,
            totalAmount, Instant.now());
    }

    public QuoteId getQuoteId() {
        return quoteId;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
