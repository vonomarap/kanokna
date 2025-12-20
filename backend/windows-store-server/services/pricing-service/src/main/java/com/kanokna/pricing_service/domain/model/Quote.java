package com.kanokna.pricing_service.domain.model;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.Currency;
import java.util.List;
import java.util.Objects;

public final class Quote {
    private final Id id;
    private final Id priceBookId;
    private final long priceBookVersion;
    private final long catalogVersion;
    private final String region;
    private final Currency currency;
    private final Money base;
    private final Money optionsTotal;
    private final Money discountTotal;
    private final Money taxTotal;
    private final Money total;
    private final List<Id> appliedCampaigns;

    public Quote(
        Id id,
        Id priceBookId,
        long priceBookVersion,
        long catalogVersion,
        String region,
        Currency currency,
        Money base,
        Money optionsTotal,
        Money discountTotal,
        Money taxTotal,
        Money total,
        List<Id> appliedCampaigns
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.priceBookId = Objects.requireNonNull(priceBookId, "priceBookId");
        this.priceBookVersion = priceBookVersion;
        this.catalogVersion = catalogVersion;
        this.region = Objects.requireNonNull(region, "region");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.base = Objects.requireNonNull(base, "base");
        this.optionsTotal = Objects.requireNonNull(optionsTotal, "optionsTotal");
        this.discountTotal = Objects.requireNonNull(discountTotal, "discountTotal");
        this.taxTotal = Objects.requireNonNull(taxTotal, "taxTotal");
        this.total = Objects.requireNonNull(total, "total");
        this.appliedCampaigns = List.copyOf(appliedCampaigns == null ? List.of() : appliedCampaigns);
    }

    public Id id() {
        return id;
    }

    public Id priceBookId() {
        return priceBookId;
    }

    public long priceBookVersion() {
        return priceBookVersion;
    }

    public long catalogVersion() {
        return catalogVersion;
    }

    public String region() {
        return region;
    }

    public Currency currency() {
        return currency;
    }

    public Money base() {
        return base;
    }

    public Money optionsTotal() {
        return optionsTotal;
    }

    public Money discountTotal() {
        return discountTotal;
    }

    public Money taxTotal() {
        return taxTotal;
    }

    public Money total() {
        return total;
    }

    public List<Id> appliedCampaigns() {
        return appliedCampaigns;
    }
}
