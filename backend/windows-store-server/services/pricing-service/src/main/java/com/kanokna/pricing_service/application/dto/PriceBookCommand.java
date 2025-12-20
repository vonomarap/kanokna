package com.kanokna.pricing_service.application.dto;

import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.Currency;
import java.util.Map;
import java.util.Objects;

public record PriceBookCommand(
    Id id,
    String region,
    Currency currency,
    PriceBookStatus status,
    Map<String, Money> basePrices,
    Map<String, Money> optionPremiums
) {
    public PriceBookCommand {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(basePrices, "basePrices");
        Objects.requireNonNull(optionPremiums, "optionPremiums");
    }
}
