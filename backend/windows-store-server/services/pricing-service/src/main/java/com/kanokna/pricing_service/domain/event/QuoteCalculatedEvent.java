package com.kanokna.pricing_service.domain.event;

import com.kanokna.pricing_service.domain.model.Money;
import com.kanokna.pricing_service.domain.model.Quote;
import com.kanokna.pricing_service.domain.model.QuoteId;
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
    private final Money basePrice;
    private final Money total;
    private final Money discount;
    private final String promoCode;
    private final Instant validUntil;
    private final Instant calculatedAt;

    private QuoteCalculatedEvent(QuoteId quoteId, String productTemplateId, String currency,
                                Money basePrice, Money total, Money discount,
                                String promoCode, Instant validUntil, Instant calculatedAt) {
        this.quoteId = Objects.requireNonNull(quoteId);
        this.productTemplateId = Objects.requireNonNull(productTemplateId);
        this.currency = Objects.requireNonNull(currency);
        this.basePrice = Objects.requireNonNull(basePrice);
        this.total = Objects.requireNonNull(total);
        this.discount = Objects.requireNonNull(discount);
        this.promoCode = promoCode;
        this.validUntil = Objects.requireNonNull(validUntil);
        this.calculatedAt = Objects.requireNonNull(calculatedAt);
    }

    public static QuoteCalculatedEvent of(Quote quote, String promoCode) {
        return new QuoteCalculatedEvent(
            quote.getQuoteId(),
            quote.getProductTemplateId(),
            quote.getTotal().getCurrency(),
            quote.getBasePrice(),
            quote.getTotal(),
            quote.getDiscount(),
            promoCode,
            quote.getValidUntil(),
            Instant.now()
        );
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

    public Money getBasePrice() {
        return basePrice;
    }

    public Money getTotal() {
        return total;
    }

    public Money getDiscount() {
        return discount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
