package com.kanokna.pricing.adapters.out.kafka;

import org.springframework.stereotype.Component;
import com.google.protobuf.Timestamp;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.EventMetadata;
import com.kanokna.common.v1.Money;
import com.kanokna.pricing.v1.QuoteCalculatedEvent;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Protobuf serializer for pricing events.
 */
@Component
public class EventSerializer {

    public QuoteCalculatedEvent toQuoteCalculatedEvent(com.kanokna.pricing.domain.event.QuoteCalculatedEvent event) {
        return QuoteCalculatedEvent.newBuilder()
            .setMetadata(buildMetadata(event))
            .setQuoteId(event.getQuoteId().toString())
            .setProductTemplateId(event.getProductTemplateId())
            .setBasePrice(toMoney(event.getBasePrice()))
            .setTotal(toMoney(event.getTotal()))
            .setCurrency(event.getCurrency())
            .setPromoCode(event.getPromoCode() == null ? "" : event.getPromoCode())
            .setDiscount(toMoney(event.getDiscount()))
            .setValidUntil(toTimestamp(event.getValidUntil()))
            .setCalculatedAt(toTimestamp(event.getCalculatedAt()))
            .build();
    }

    private EventMetadata buildMetadata(com.kanokna.pricing.domain.event.QuoteCalculatedEvent event) {
        return EventMetadata.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setOccurredAt(toTimestamp(Instant.now()))
            .setAggregateId(event.getQuoteId().toString())
            .setAggregateType("Quote")
            .setVersion(1)
            .build();
    }

    private Money toMoney(com.kanokna.pricing.domain.model.Money money) {
        long minor = money.getAmount()
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        return Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(mapCurrency(money.getCurrency()))
            .build();
    }

    private Currency mapCurrency(String currency) {
        if ("RUB".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_RUB;
        }
        if ("EUR".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_EUR;
        }
        if ("USD".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_USD;
        }
        return Currency.CURRENCY_UNSPECIFIED;
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
}
