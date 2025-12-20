package com.kanokna.pricing_service.domain.event;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.event.DomainEvent;
import com.kanokna.shared.money.Money;

import java.time.Instant;
import java.util.Objects;

public final class QuoteCalculatedEvent implements DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final Id priceBookId;
    private final long priceBookVersion;
    private final Id quoteId;
    private final Money total;
    private final String currencyCode;

    private QuoteCalculatedEvent(
        String eventId,
        Instant occurredAt,
        Id priceBookId,
        long priceBookVersion,
        Id quoteId,
        Money total
    ) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        this.priceBookId = Objects.requireNonNull(priceBookId, "priceBookId");
        this.priceBookVersion = priceBookVersion;
        this.quoteId = Objects.requireNonNull(quoteId, "quoteId");
        this.total = Objects.requireNonNull(total, "total");
        this.currencyCode = total.getCurrency().getCurrencyCode();
    }

    public static QuoteCalculatedEvent of(Id priceBookId, long priceBookVersion, Id quoteId, Money total) {
        return new QuoteCalculatedEvent(Id.random().value(), Instant.now(), priceBookId, priceBookVersion, quoteId, total);
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Id priceBookId() {
        return priceBookId;
    }

    public long priceBookVersion() {
        return priceBookVersion;
    }

    public Id quoteId() {
        return quoteId;
    }

    public Money total() {
        return total;
    }

    public String currencyCode() {
        return currencyCode;
    }
}
