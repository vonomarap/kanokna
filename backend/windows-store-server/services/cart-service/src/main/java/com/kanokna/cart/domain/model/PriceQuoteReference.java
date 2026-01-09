package com.kanokna.cart.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Reference to a pricing-service quote.
 */
public record PriceQuoteReference(String quoteId, Instant validUntil) {
    public PriceQuoteReference {
        Objects.requireNonNull(quoteId, "quoteId cannot be null");
    }

    public boolean isStale(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        return validUntil == null || now.isAfter(validUntil);
    }
}
