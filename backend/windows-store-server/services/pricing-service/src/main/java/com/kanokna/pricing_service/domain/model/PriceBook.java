package com.kanokna.pricing_service.domain.model;

import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PriceBook {
    private final Id id;
    private final String region;
    private final Currency currency;
    private final PriceBookStatus status;
    private final long version;
    private final Map<String, Money> basePricesByItem;
    private final Map<OptionPremiumKey, Money> optionPremiums;

    public PriceBook(
        Id id,
        String region,
        Currency currency,
        PriceBookStatus status,
        long version,
        Map<String, Money> basePricesByItem,
        Map<OptionPremiumKey, Money> optionPremiums
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.region = requireText(region, "region");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.status = Objects.requireNonNull(status, "status");
        if (version < 0) {
            throw new IllegalArgumentException("version must be non-negative");
        }
        this.version = version;
        this.basePricesByItem = Collections.unmodifiableMap(validateBasePrices(basePricesByItem));
        this.optionPremiums = Collections.unmodifiableMap(validateOptionPremiums(optionPremiums));
    }

    public Id id() {
        return id;
    }

    public String region() {
        return region;
    }

    public Currency currency() {
        return currency;
    }

    public PriceBookStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public boolean isActive() {
        return status == PriceBookStatus.ACTIVE;
    }

    public Money basePriceFor(String itemCode) {
        Money price = basePricesByItem.get(itemCode);
        if (price == null) {
            throw new PricingDomainException("Base price missing for item: " + itemCode);
        }
        ensureCurrency(price);
        return price;
    }

    public Money optionPremiumFor(String attributeCode, String optionCode) {
        OptionPremiumKey key = new OptionPremiumKey(attributeCode, optionCode);
        Money premium = optionPremiums.get(key);
        if (premium == null) {
            return Money.zero(currency);
        }
        ensureCurrency(premium);
        return premium;
    }

    public Map<String, Money> basePricesByItem() {
        return basePricesByItem;
    }

    public Map<OptionPremiumKey, Money> optionPremiums() {
        return optionPremiums;
    }

    public PriceBook publish() {
        if (status == PriceBookStatus.DEPRECATED) {
            throw new PricingDomainException("Cannot publish deprecated price book");
        }
        if (status == PriceBookStatus.ACTIVE) {
            return this;
        }
        return new PriceBook(id, region, currency, PriceBookStatus.ACTIVE, version + 1, basePricesByItem, optionPremiums);
    }

    private void ensureCurrency(Money money) {
        if (!money.getCurrency().equals(currency)) {
            throw new PricingDomainException("Currency mismatch in price book: expected %s got %s"
                .formatted(currency.getCurrencyCode(), money.getCurrency().getCurrencyCode()));
        }
        if (money.isNegative()) {
            throw new PricingDomainException("Price cannot be negative");
        }
    }

    private Map<String, Money> validateBasePrices(Map<String, Money> source) {
        Objects.requireNonNull(source, "basePricesByItem");
        Map<String, Money> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new PricingDomainException("Item code cannot be blank");
            }
            ensureCurrency(value);
            copy.put(key.trim(), value);
        });
        return copy;
    }

    private Map<OptionPremiumKey, Money> validateOptionPremiums(Map<OptionPremiumKey, Money> source) {
        Objects.requireNonNull(source, "optionPremiums");
        Map<OptionPremiumKey, Money> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            ensureCurrency(value);
            copy.put(key, value);
        });
        return copy;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must be provided");
        }
        return value.trim();
    }
}
