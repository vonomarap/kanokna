package com.kanokna.pricing_service.domain.event;

import com.kanokna.pricing_service.domain.model.PromoCodeId;
import com.kanokna.pricing_service.domain.model.QuoteId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a promo code is successfully applied to a quote.
 */
public class PromoCodeAppliedEvent {
    private final PromoCodeId promoCodeId;
    private final String code;
    private final QuoteId quoteId;
    private final String discountAmount;
    private final Instant appliedAt;

    private PromoCodeAppliedEvent(PromoCodeId promoCodeId, String code, QuoteId quoteId,
                                 String discountAmount, Instant appliedAt) {
        this.promoCodeId = Objects.requireNonNull(promoCodeId);
        this.code = Objects.requireNonNull(code);
        this.quoteId = Objects.requireNonNull(quoteId);
        this.discountAmount = Objects.requireNonNull(discountAmount);
        this.appliedAt = Objects.requireNonNull(appliedAt);
    }

    public static PromoCodeAppliedEvent of(PromoCodeId promoCodeId, String code,
                                          QuoteId quoteId, String discountAmount) {
        return new PromoCodeAppliedEvent(promoCodeId, code, quoteId, discountAmount, Instant.now());
    }

    public PromoCodeId getPromoCodeId() {
        return promoCodeId;
    }

    public String getCode() {
        return code;
    }

    public QuoteId getQuoteId() {
        return quoteId;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }
}

