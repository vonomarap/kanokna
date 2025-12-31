package com.kanokna.pricing.domain.event;

import com.kanokna.pricing.domain.model.PriceBookId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a price book is published and made active.
 */
public class PriceBookPublishedEvent {
    private final PriceBookId priceBookId;
    private final String productTemplateId;
    private final int versionNumber;
    private final Instant publishedAt;
    private final String publishedBy;

    private PriceBookPublishedEvent(PriceBookId priceBookId, String productTemplateId,
                                   int versionNumber, Instant publishedAt, String publishedBy) {
        this.priceBookId = Objects.requireNonNull(priceBookId);
        this.productTemplateId = Objects.requireNonNull(productTemplateId);
        this.versionNumber = versionNumber;
        this.publishedAt = Objects.requireNonNull(publishedAt);
        this.publishedBy = publishedBy;
    }

    public static PriceBookPublishedEvent of(PriceBookId priceBookId, String productTemplateId,
                                            int versionNumber, String publishedBy) {
        return new PriceBookPublishedEvent(priceBookId, productTemplateId, versionNumber,
            Instant.now(), publishedBy);
    }

    public PriceBookId getPriceBookId() {
        return priceBookId;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }
}
