package com.kanokna.pricing_service.domain.model;

import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.pricing_service.domain.service.DecisionTrace;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;
import com.kanokna.shared.money.MoneyRoundingPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Campaign {
    private final Id id;
    private final String name;
    private final CampaignStatus status;
    private final BigDecimal percentOff;
    private final Instant startsAt;
    private final Instant endsAt;

    public Campaign(Id id, String name, CampaignStatus status, BigDecimal percentOff, Instant startsAt, Instant endsAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = requireText(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.percentOff = requirePercent(percentOff);
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        validateSchedule();
    }

    public Id id() {
        return id;
    }

    public String name() {
        return name;
    }

    public CampaignStatus status() {
        return status;
    }

    public BigDecimal percentOff() {
        return percentOff;
    }

    public Instant startsAt() {
        return startsAt;
    }

    public Instant endsAt() {
        return endsAt;
    }

    public boolean isActive(Instant now) {
        if (status != CampaignStatus.ACTIVE) {
            return false;
        }
        if (startsAt != null && now.isBefore(startsAt)) {
            return false;
        }
        return endsAt == null || !now.isAfter(endsAt);
    }

    public Money discountFor(Money subtotal, MoneyRoundingPolicy policy, Instant now, DecisionTrace.TraceCollector traces) {
        if (!isActive(now)) {
            traces.trace("PRICE-DISCOUNT", "CAMPAIGN_SKIPPED", "campaign=" + id.value());
            return Money.zero(subtotal.getCurrency());
        }
        if (subtotal.isNegative() || subtotal.isZero()) {
            traces.trace("PRICE-DISCOUNT", "NO_SUBTOTAL", "campaign=" + id.value());
            return Money.zero(subtotal.getCurrency());
        }
        Money discount = subtotal.multiplyBy(percentOff, policy);
        traces.trace("PRICE-DISCOUNT", "CAMPAIGN_APPLIED", "campaign=%s percent=%.3f".formatted(id.value(), percentOff));
        return discount;
    }

    private void validateSchedule() {
        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new PricingDomainException("Campaign schedule invalid: starts after ends");
        }
    }

    private BigDecimal requirePercent(BigDecimal value) {
        Objects.requireNonNull(value, "percentOff");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new PricingDomainException("percentOff must be between 0 and 1");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must be provided");
        }
        return value.trim();
    }
}
