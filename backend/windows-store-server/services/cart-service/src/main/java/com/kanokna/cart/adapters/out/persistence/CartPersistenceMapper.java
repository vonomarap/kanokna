package com.kanokna.cart.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartId;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartItemId;
import com.kanokna.cart.domain.model.CartSnapshot;
import com.kanokna.cart.domain.model.CartSnapshotItem;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.CartTotals;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.model.SnapshotId;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartPersistenceMapper {
    private final ObjectMapper objectMapper;

    public CartPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Cart toDomain(CartJpaEntity entity) {
        List<CartItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (CartItemJpaEntity itemEntity : entity.getItems()) {
                items.add(toDomain(itemEntity));
            }
        }

        CartTotals totals = toCartTotals(entity, items);
        AppliedPromoCode appliedPromoCode = toAppliedPromo(entity, totals);

        return Cart.rehydrate(
            CartId.of(entity.getCartId()),
            entity.getCustomerId(),
            entity.getSessionId(),
            entity.getStatus() == null ? CartStatus.ACTIVE : entity.getStatus(),
            appliedPromoCode,
            totals,
            items,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            Math.toIntExact(entity.getVersion())
        );
    }

    public CartJpaEntity toEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setCartId(UUID.fromString(cart.cartId().toString()));
        entity.setCustomerId(cart.customerId());
        entity.setSessionId(cart.sessionId());
        entity.setStatus(cart.status());
        entity.setCreatedAt(cart.createdAt());
        entity.setUpdatedAt(cart.updatedAt());
        entity.setVersion(cart.version());

        CartTotals totals = cart.totals();
        entity.setSubtotalAmount(totals.subtotal().getAmount());
        entity.setSubtotalCurrency(totals.subtotal().getCurrency().name());
        entity.setDiscountAmount(totals.discount().getAmount());
        entity.setTaxAmount(totals.tax().getAmount());
        entity.setTotalAmount(totals.total().getAmount());

        AppliedPromoCode promo = cart.appliedPromoCode();
        if (promo != null) {
            entity.setAppliedPromoCode(promo.code());
            entity.setPromoDiscountAmount(promo.discountAmount().getAmount());
            entity.setPromoDiscountCurrency(promo.discountAmount().getCurrency().name());
        }

        List<CartItemJpaEntity> items = new ArrayList<>();
        for (CartItem item : cart.items()) {
            CartItemJpaEntity itemEntity = toEntity(item);
            itemEntity.setCart(entity);
            items.add(itemEntity);
        }
        entity.setItems(items);
        return entity;
    }

    public CartSnapshot toDomain(CartSnapshotJpaEntity entity) {
        CartSnapshotPayload payload = readSnapshotPayload(entity.getSnapshotData());
        List<CartSnapshotItem> items = payload.items().stream()
            .map(this::toSnapshotItem)
            .toList();

        CartTotals totals = new CartTotals(
            toMoney(entity.getSubtotalAmount(), entity.getCurrency()),
            toMoney(entity.getDiscountAmount(), entity.getCurrency()),
            toMoney(entity.getTaxAmount(), entity.getCurrency()),
            toMoney(entity.getTotalAmount(), entity.getCurrency()),
            resolveItemCount(entity.getItemCount(), items)
        );

        AppliedPromoCode promo = payload.appliedPromoCode() != null
            ? toAppliedPromo(payload.appliedPromoCode())
            : null;

        return CartSnapshot.rehydrate(
            SnapshotId.of(entity.getSnapshotId()),
            CartId.of(entity.getCartId()),
            entity.getCustomerId(),
            items,
            totals,
            promo,
            entity.getCreatedAt(),
            entity.getValidUntil()
        );
    }

    public CartSnapshotJpaEntity toEntity(CartSnapshot snapshot) {
        CartSnapshotJpaEntity entity = new CartSnapshotJpaEntity();
        entity.setSnapshotId(UUID.fromString(snapshot.snapshotId().toString()));
        entity.setCartId(UUID.fromString(snapshot.cartId().toString()));
        entity.setCustomerId(snapshot.customerId());
        entity.setCreatedAt(snapshot.createdAt());
        entity.setValidUntil(snapshot.validUntil());
        entity.setSnapshotData(writeSnapshotPayload(snapshot));

        CartTotals totals = snapshot.totals();
        entity.setSubtotalAmount(totals.subtotal().getAmount());
        entity.setDiscountAmount(totals.discount().getAmount());
        entity.setTaxAmount(totals.tax().getAmount());
        entity.setTotalAmount(totals.total().getAmount());
        entity.setCurrency(totals.total().getCurrency().name());
        entity.setItemCount(totals.itemCount());

        AppliedPromoCode promo = snapshot.appliedPromoCode();
        entity.setAppliedPromoCode(promo != null ? promo.code() : null);
        return entity;
    }

    private CartItem toDomain(CartItemJpaEntity entity) {
        String quoteId = entity.getQuoteId();
        if (quoteId == null) {
            throw new IllegalStateException("quote_id is required for cart item " + entity.getItemId());
        }
        PriceQuoteReference quoteReference = new PriceQuoteReference(quoteId, entity.getQuoteValidUntil());

        ConfigurationSnapshot snapshot = readConfigurationSnapshot(entity.getConfigurationSnapshot());
        Money unitPrice = toMoney(entity.getUnitPriceAmount(), entity.getUnitPriceCurrency());
        ValidationStatus status = entity.getValidationStatus() == null
            ? ValidationStatus.UNKNOWN
            : entity.getValidationStatus();

        return CartItem.rehydrate(
            CartItemId.of(entity.getItemId()),
            entity.getProductTemplateId(),
            entity.getProductName(),
            entity.getProductFamily(),
            snapshot,
            entity.getConfigurationHash(),
            entity.getQuantity(),
            unitPrice,
            quoteReference,
            status,
            entity.getValidationMessage(),
            entity.getThumbnailUrl(),
            false,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private CartItemJpaEntity toEntity(CartItem item) {
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setItemId(UUID.fromString(item.itemId().toString()));
        entity.setProductTemplateId(item.productTemplateId());
        entity.setProductName(item.productName());
        entity.setProductFamily(item.productFamily());
        entity.setThumbnailUrl(item.thumbnailUrl());
        entity.setConfigurationSnapshot(writeConfigurationSnapshot(item.configurationSnapshot()));
        entity.setConfigurationHash(item.configurationHash());
        entity.setQuantity(item.quantity());
        entity.setUnitPriceAmount(item.unitPrice().getAmount());
        entity.setUnitPriceCurrency(item.unitPrice().getCurrency().name());
        entity.setLineTotalAmount(item.lineTotal().getAmount());
        entity.setQuoteId(item.quoteReference().quoteId());
        entity.setQuoteValidUntil(item.quoteReference().validUntil());
        entity.setValidationStatus(item.validationStatus());
        entity.setValidationMessage(item.validationMessage());
        entity.setCreatedAt(item.createdAt());
        entity.setUpdatedAt(item.updatedAt());
        return entity;
    }

    private CartTotals toCartTotals(CartJpaEntity entity, List<CartItem> items) {
        Money subtotal = toMoney(entity.getSubtotalAmount(), entity.getSubtotalCurrency());
        Money discount = toMoney(entity.getDiscountAmount(), entity.getSubtotalCurrency());
        Money tax = toMoney(entity.getTaxAmount(), entity.getSubtotalCurrency());
        Money total = toMoney(entity.getTotalAmount(), entity.getSubtotalCurrency());
        int itemCount = items.stream().mapToInt(CartItem::quantity).sum();
        return new CartTotals(subtotal, discount, tax, total, itemCount);
    }

    private AppliedPromoCode toAppliedPromo(CartJpaEntity entity, CartTotals totals) {
        if (entity.getAppliedPromoCode() == null) {
            return null;
        }
        String currency = entity.getPromoDiscountCurrency() != null
            ? entity.getPromoDiscountCurrency()
            : totals.discount().getCurrency().name();
        Money discount = toMoney(entity.getPromoDiscountAmount(), currency);
        return new AppliedPromoCode(
            entity.getAppliedPromoCode(),
            discount,
            null,
            entity.getUpdatedAt() != null ? entity.getUpdatedAt() : Instant.now()
        );
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        Currency currency = resolveCurrency(currencyCode);
        BigDecimal resolved = amount == null ? BigDecimal.ZERO : amount;
        return Money.of(resolved, currency);
    }

    private Currency resolveCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return Currency.RUB;
        }
        try {
            return Currency.valueOf(currencyCode);
        } catch (IllegalArgumentException ex) {
            return Currency.RUB;
        }
    }

    private int resolveItemCount(int storedCount, List<CartSnapshotItem> items) {
        if (storedCount > 0) {
            return storedCount;
        }
        return items.stream().mapToInt(CartSnapshotItem::quantity).sum();
    }

    private ConfigurationSnapshot readConfigurationSnapshot(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("configuration_snapshot is required");
        }
        try {
            return objectMapper.readValue(json, ConfigurationSnapshot.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse configuration snapshot", ex);
        }
    }

    private String writeConfigurationSnapshot(ConfigurationSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize configuration snapshot", ex);
        }
    }

    private CartSnapshotPayload readSnapshotPayload(String json) {
        if (json == null || json.isBlank()) {
            return new CartSnapshotPayload(List.of(), null);
        }
        try {
            return objectMapper.readValue(json, CartSnapshotPayload.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse snapshot payload", ex);
        }
    }

    private String writeSnapshotPayload(CartSnapshot snapshot) {
        List<CartSnapshotItemPayload> items = snapshot.items().stream()
            .map(this::toSnapshotItemPayload)
            .toList();
        AppliedPromoPayload promoPayload = snapshot.appliedPromoCode() != null
            ? toPromoPayload(snapshot.appliedPromoCode())
            : null;
        CartSnapshotPayload payload = new CartSnapshotPayload(items, promoPayload);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize snapshot payload", ex);
        }
    }

    private CartSnapshotItem toSnapshotItem(CartSnapshotItemPayload payload) {
        PriceQuoteReference quoteReference = new PriceQuoteReference(
            payload.quoteReference().quoteId(),
            payload.quoteReference().validUntil()
        );
        return new CartSnapshotItem(
            payload.itemId(),
            payload.productTemplateId(),
            payload.productName(),
            payload.productFamily(),
            payload.configurationSnapshot(),
            payload.configurationHash(),
            payload.quantity(),
            toMoney(payload.unitPrice().amount(), payload.unitPrice().currency()),
            toMoney(payload.lineTotal().amount(), payload.lineTotal().currency()),
            quoteReference,
            payload.thumbnailUrl()
        );
    }

    private CartSnapshotItemPayload toSnapshotItemPayload(CartSnapshotItem item) {
        return new CartSnapshotItemPayload(
            item.itemId(),
            item.productTemplateId(),
            item.productName(),
            item.productFamily(),
            item.configurationSnapshot(),
            item.configurationHash(),
            item.quantity(),
            new MoneyPayload(item.unitPrice().getAmount(), item.unitPrice().getCurrency().name()),
            new MoneyPayload(item.lineTotal().getAmount(), item.lineTotal().getCurrency().name()),
            new QuoteReferencePayload(item.quoteReference().quoteId(), item.quoteReference().validUntil()),
            item.thumbnailUrl()
        );
    }

    private AppliedPromoCode toAppliedPromo(AppliedPromoPayload payload) {
        Money discount = toMoney(payload.discountAmount().amount(), payload.discountAmount().currency());
        return new AppliedPromoCode(payload.code(), discount, payload.description(), payload.appliedAt());
    }

    private AppliedPromoPayload toPromoPayload(AppliedPromoCode promo) {
        return new AppliedPromoPayload(
            promo.code(),
            new MoneyPayload(promo.discountAmount().getAmount(), promo.discountAmount().getCurrency().name()),
            promo.description(),
            promo.appliedAt()
        );
    }

    private record CartSnapshotPayload(
        List<CartSnapshotItemPayload> items,
        AppliedPromoPayload appliedPromoCode
    ) {
    }

    private record CartSnapshotItemPayload(
        String itemId,
        String productTemplateId,
        String productName,
        String productFamily,
        ConfigurationSnapshot configurationSnapshot,
        String configurationHash,
        int quantity,
        MoneyPayload unitPrice,
        MoneyPayload lineTotal,
        QuoteReferencePayload quoteReference,
        String thumbnailUrl
    ) {
    }

    private record AppliedPromoPayload(
        String code,
        MoneyPayload discountAmount,
        String description,
        Instant appliedAt
    ) {
    }

    private record MoneyPayload(BigDecimal amount, String currency) {
    }

    private record QuoteReferencePayload(String quoteId, Instant validUntil) {
    }
}
