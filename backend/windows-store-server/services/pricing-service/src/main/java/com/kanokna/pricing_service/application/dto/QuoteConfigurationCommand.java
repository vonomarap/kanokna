package com.kanokna.pricing_service.application.dto;

import com.kanokna.shared.core.Id;

import java.util.Currency;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record QuoteConfigurationCommand(
    Id priceBookId,
    String region,
    Currency currency,
    String itemCode,
    Map<String, String> optionSelections,
    String customerSegment,
    long catalogVersion,
    boolean includeTax,
    String idempotencyKey
) {
    public QuoteConfigurationCommand {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(itemCode, "itemCode");
        Objects.requireNonNull(optionSelections, "optionSelections");
        if (itemCode.isBlank()) {
            throw new IllegalArgumentException("itemCode must be provided");
        }
    }

    public Optional<Id> priceBookIdOptional() {
        return Optional.ofNullable(priceBookId);
    }
}
