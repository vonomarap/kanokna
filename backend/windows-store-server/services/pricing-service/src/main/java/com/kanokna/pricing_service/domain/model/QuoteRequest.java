package com.kanokna.pricing_service.domain.model;

import com.kanokna.shared.core.Id;

import java.util.Currency;
import java.util.Map;
import java.util.Objects;

public record QuoteRequest(
    Id priceBookId,
    String region,
    Currency currency,
    String itemCode,
    Map<String, String> optionSelections,
    String customerSegment,
    long catalogVersion,
    boolean includeTax
) {
    public QuoteRequest {
        Objects.requireNonNull(priceBookId, "priceBookId");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(itemCode, "itemCode");
        Objects.requireNonNull(optionSelections, "optionSelections");
        if (itemCode.isBlank()) {
            throw new IllegalArgumentException("itemCode must be provided");
        }
    }
}
