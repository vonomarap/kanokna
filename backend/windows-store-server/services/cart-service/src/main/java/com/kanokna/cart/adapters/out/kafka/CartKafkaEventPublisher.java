package com.kanokna.cart.adapters.out.kafka;

import com.google.protobuf.Timestamp;
import com.kanokna.cart.application.port.out.CartEventPublisher;
import com.kanokna.cart.application.port.out.EventPublisher;
import com.kanokna.cart.domain.event.CartAbandonedEvent;
import com.kanokna.cart.domain.event.CartCheckedOutEvent;
import com.kanokna.cart.domain.event.CartClearedEvent;
import com.kanokna.cart.domain.event.CartCreatedEvent;
import com.kanokna.cart.domain.event.CartItemAddedEvent;
import com.kanokna.cart.domain.event.CartItemRemovedEvent;
import com.kanokna.cart.domain.event.CartItemUpdatedEvent;
import com.kanokna.cart.domain.event.CartMergedEvent;
import com.kanokna.cart.domain.event.CartPricesRefreshedEvent;
import com.kanokna.cart.domain.event.PromoCodeAppliedEvent;
import com.kanokna.cart.domain.event.PromoCodeRemovedEvent;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Money;
import com.kanokna.shared.event.DomainEvent;
import io.opentelemetry.api.trace.Span;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka publisher for cart domain events.
 */
@Component
public class CartKafkaEventPublisher implements EventPublisher, CartEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CartKafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public <T> void publish(String topic, T event) {
        String key = event instanceof DomainEvent domainEvent ? domainEvent.eventId() : null;
        Object payload = event instanceof DomainEvent domainEvent
            ? toProto(domainEvent)
            : event;
        if (key != null) {
            kafkaTemplate.send(topic, key, payload);
        } else {
            kafkaTemplate.send(topic, payload);
        }
    }

    @Override
    public void publish(DomainEvent event) {
        Object payload = toProto(event);
        kafkaTemplate.send(resolveTopic(event), event.eventId(), payload);
    }

    private Object toProto(DomainEvent event) {
        String traceId = resolveTraceId();
        if (event instanceof CartCreatedEvent created) {
            return com.kanokna.cart.v1.event.CartCreatedEvent.newBuilder()
                .setEventId(created.eventId())
                .setCartId(created.cartId())
                .setCustomerId(valueOrEmpty(created.customerId()))
                .setSessionId(valueOrEmpty(created.sessionId()))
                .setCreatedAt(toTimestamp(created.cartCreatedAt()))
                .setIsAnonymous(created.anonymous())
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartItemAddedEvent added) {
            return com.kanokna.cart.v1.event.CartItemAddedEvent.newBuilder()
                .setEventId(added.eventId())
                .setCartId(added.cartId())
                .setCustomerId(valueOrEmpty(added.customerId()))
                .setItemId(added.itemId())
                .setProductTemplateId(added.productTemplateId())
                .setProductName(added.productName())
                .setProductFamily(valueOrEmpty(added.productFamily()))
                .setQuantity(added.quantity())
                .setUnitPrice(toMoney(added.unitPrice()))
                .setLineTotal(toMoney(added.lineTotal()))
                .setConfigurationHash(added.configurationHash())
                .setOccurredAt(toTimestamp(added.occurredAt()))
                .setCartItemCount(added.cartItemCount())
                .setCartSubtotal(toMoney(added.cartSubtotal()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartItemUpdatedEvent updated) {
            return com.kanokna.cart.v1.event.CartItemUpdatedEvent.newBuilder()
                .setEventId(updated.eventId())
                .setCartId(updated.cartId())
                .setCustomerId(valueOrEmpty(updated.customerId()))
                .setItemId(updated.itemId())
                .setOldQuantity(updated.oldQuantity())
                .setNewQuantity(updated.newQuantity())
                .setOldLineTotal(toMoney(updated.oldLineTotal()))
                .setNewLineTotal(toMoney(updated.newLineTotal()))
                .setOccurredAt(toTimestamp(updated.occurredAt()))
                .setCartSubtotal(toMoney(updated.cartSubtotal()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartItemRemovedEvent removed) {
            return com.kanokna.cart.v1.event.CartItemRemovedEvent.newBuilder()
                .setEventId(removed.eventId())
                .setCartId(removed.cartId())
                .setCustomerId(valueOrEmpty(removed.customerId()))
                .setItemId(removed.itemId())
                .setProductTemplateId(removed.productTemplateId())
                .setProductName(removed.productName())
                .setQuantityRemoved(removed.quantityRemoved())
                .setLineTotalRemoved(toMoney(removed.lineTotalRemoved()))
                .setOccurredAt(toTimestamp(removed.occurredAt()))
                .setCartItemCount(removed.cartItemCount())
                .setCartSubtotal(toMoney(removed.cartSubtotal()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof PromoCodeAppliedEvent promoApplied) {
            return com.kanokna.cart.v1.event.PromoCodeAppliedEvent.newBuilder()
                .setEventId(promoApplied.eventId())
                .setCartId(promoApplied.cartId())
                .setCustomerId(valueOrEmpty(promoApplied.customerId()))
                .setPromoCode(promoApplied.promoCode())
                .setDiscountAmount(toMoney(promoApplied.discountAmount()))
                .setSubtotal(toMoney(promoApplied.subtotal()))
                .setNewTotal(toMoney(promoApplied.newTotal()))
                .setDiscountPercent(promoApplied.discountPercent())
                .setOccurredAt(toTimestamp(promoApplied.occurredAt()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof PromoCodeRemovedEvent promoRemoved) {
            return com.kanokna.cart.v1.event.PromoCodeRemovedEvent.newBuilder()
                .setEventId(promoRemoved.eventId())
                .setCartId(promoRemoved.cartId())
                .setCustomerId(valueOrEmpty(promoRemoved.customerId()))
                .setPromoCode(promoRemoved.promoCode())
                .setDiscountRemoved(toMoney(promoRemoved.discountRemoved()))
                .setNewTotal(toMoney(promoRemoved.newTotal()))
                .setOccurredAt(toTimestamp(promoRemoved.occurredAt()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartMergedEvent merged) {
            return com.kanokna.cart.v1.event.CartMergedEvent.newBuilder()
                .setEventId(merged.eventId())
                .setSourceCartId(merged.sourceCartId())
                .setTargetCartId(merged.targetCartId())
                .setCustomerId(valueOrEmpty(merged.customerId()))
                .setItemsMergedCount(merged.itemsMergedCount())
                .setItemsQuantitySummed(merged.itemsQuantitySummed())
                .setItemsAddedNew(merged.itemsAddedNew())
                .setPromoCodeSource(valueOrEmpty(merged.promoCodeSource()))
                .setPromoCode(valueOrEmpty(merged.promoCode()))
                .setFinalTotal(toMoney(merged.finalTotal()))
                .setOccurredAt(toTimestamp(merged.occurredAt()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartClearedEvent cleared) {
            return com.kanokna.cart.v1.event.CartClearedEvent.newBuilder()
                .setEventId(cleared.eventId())
                .setCartId(cleared.cartId())
                .setCustomerId(valueOrEmpty(cleared.customerId()))
                .setSessionId(valueOrEmpty(cleared.sessionId()))
                .setItemsRemoved(cleared.itemsRemoved())
                .setClearedSubtotal(toMoney(cleared.clearedSubtotal()))
                .setPromoCodeRemoved(cleared.promoCodeRemoved())
                .setOccurredAt(toTimestamp(cleared.occurredAt()))
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartCheckedOutEvent checkout) {
            return com.kanokna.cart.v1.event.CartCheckedOutEvent.newBuilder()
                .setEventId(checkout.eventId())
                .setCartId(checkout.cartId())
                .setSnapshotId(checkout.snapshotId())
                .setCustomerId(valueOrEmpty(checkout.customerId()))
                .setItemCount(checkout.itemCount())
                .setTotal(toMoney(checkout.total()))
                .setAppliedPromoCode(valueOrEmpty(checkout.appliedPromoCode()))
                .setDiscountAmount(toMoney(checkout.discountAmount()))
                .setOccurredAt(toTimestamp(checkout.occurredAt()))
                .setCartAgeSeconds(checkout.cartAgeSeconds())
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartAbandonedEvent abandoned) {
            long hoursInactive = Duration.between(abandoned.lastActivity(), abandoned.occurredAt()).toHours();
            return com.kanokna.cart.v1.event.CartAbandonedEvent.newBuilder()
                .setEventId(abandoned.eventId())
                .setCartId(abandoned.cartId())
                .setCustomerId(valueOrEmpty(abandoned.customerId()))
                .setCustomerEmail("")
                .setLanguage("")
                .setLastActivity(toTimestamp(abandoned.lastActivity()))
                .setItemCount(abandoned.itemCount())
                .setSubtotal(toMoney(abandoned.subtotal()))
                .setOccurredAt(toTimestamp(abandoned.occurredAt()))
                .setHoursInactive((int) hoursInactive)
                .setTraceId(traceId)
                .build();
        }
        if (event instanceof CartPricesRefreshedEvent refreshed) {
            return com.kanokna.cart.v1.event.CartPricesRefreshedEvent.newBuilder()
                .setEventId(refreshed.eventId())
                .setCartId(refreshed.cartId())
                .setCustomerId(valueOrEmpty(refreshed.customerId()))
                .setItemsWithChanges(refreshed.itemsWithChanges())
                .setPreviousTotal(toMoney(refreshed.previousTotal()))
                .setNewTotal(toMoney(refreshed.newTotal()))
                .setTotalChange(toMoney(refreshed.totalChange()))
                .setChangePercent(refreshed.changePercent())
                .setRefreshTrigger(valueOrEmpty(refreshed.refreshTrigger()))
                .setOccurredAt(toTimestamp(refreshed.occurredAt()))
                .setTraceId(traceId)
                .build();
        }
        return event;
    }

    private String resolveTopic(DomainEvent event) {
        if (event instanceof CartCreatedEvent) {
            return "cart.created";
        }
        if (event instanceof CartItemAddedEvent) {
            return "cart.item.added";
        }
        if (event instanceof CartItemUpdatedEvent) {
            return "cart.item.updated";
        }
        if (event instanceof CartItemRemovedEvent) {
            return "cart.item.removed";
        }
        if (event instanceof PromoCodeAppliedEvent) {
            return "cart.promo.applied";
        }
        if (event instanceof PromoCodeRemovedEvent) {
            return "cart.promo.removed";
        }
        if (event instanceof CartMergedEvent) {
            return "cart.merged";
        }
        if (event instanceof CartAbandonedEvent) {
            return "cart.abandoned";
        }
        if (event instanceof CartCheckedOutEvent) {
            return "cart.checkout";
        }
        if (event instanceof CartClearedEvent) {
            return "cart.cleared";
        }
        if (event instanceof CartPricesRefreshedEvent) {
            return "cart.prices.refreshed";
        }
        return "cart.unknown";
    }

    private Money toMoney(com.kanokna.shared.money.Money money) {
        if (money == null) {
            return Money.newBuilder()
                .setAmountMinor(0)
                .setCurrency(Currency.CURRENCY_UNSPECIFIED)
                .build();
        }
        int scale = money.getCurrency().getDefaultScale();
        long minor = money.getAmount()
            .movePointRight(scale)
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        return Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(mapCurrency(money.getCurrency()))
            .build();
    }

    private Currency mapCurrency(com.kanokna.shared.money.Currency currency) {
        if (currency == null) {
            return Currency.CURRENCY_UNSPECIFIED;
        }
        return switch (currency) {
            case RUB -> Currency.CURRENCY_RUB;
            case EUR -> Currency.CURRENCY_EUR;
            case USD -> Currency.CURRENCY_USD;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    private String resolveTraceId() {
        Span span = Span.current();
        if (span != null && span.getSpanContext().isValid()) {
            return span.getSpanContext().getTraceId();
        }
        String mdcTrace = MDC.get("traceId");
        return mdcTrace == null ? "" : mdcTrace;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
