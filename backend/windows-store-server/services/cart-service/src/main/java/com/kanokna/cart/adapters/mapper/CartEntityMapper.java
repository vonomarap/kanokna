package com.kanokna.cart.adapters.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.cart.adapters.out.persistence.CartItemJpaEntity;
import com.kanokna.cart.adapters.out.persistence.CartJpaEntity;
import com.kanokna.cart.domain.model.*;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

/**
 * MapStruct mapper for Cart entity ↔ domain conversions.
 */
@Mapper(componentModel = "spring", uses = {MoneyMapper.class, CartIdMapper.class})
public interface CartEntityMapper {

    @Mapping(target = "cartId", source = "cartId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "appliedPromoCode", source = "appliedPromoCode")
    @Mapping(target = "promoDiscountAmount", source = "entity", qualifiedByName = "promoDiscountAmount")
    @Mapping(target = "promoDiscountCurrency", source = "entity", qualifiedByName = "promoDiscountCurrency")
    @Mapping(target = "subtotalAmount", source = "entity", qualifiedByName = "subtotalAmount")
    @Mapping(target = "subtotalCurrency", source = "subtotalCurrency")
    @Mapping(target = "discountAmount", source = "entity", qualifiedByName = "discountAmountFromEntity")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    CartJpaEntity toEntity(Cart cart);

    @Named("subtotalAmount")
    default java.math.BigDecimal subtotalAmount(CartJpaEntity entity) {
        return entity.getSubtotalAmount();
    }

    @Named("discountAmountFromEntity")
    default java.math.BigDecimal discountAmountFromEntity(CartJpaEntity entity) {
        return entity.getDiscountAmount();
    }

    @Named("promoDiscountAmount")
    default java.math.BigDecimal promoDiscountAmount(CartJpaEntity entity) {
        return entity.getPromoDiscountAmount();
    }

    @Named("promoDiscountCurrency")
    default String promoDiscountCurrency(CartJpaEntity entity) {
        return entity.getPromoDiscountCurrency();
    }

    default void updateEntityFromDomain(Cart cart, @MappingTarget CartJpaEntity entity, ObjectMapper objectMapper) {
        entity.setCartId(cart.cartId().value());
        entity.setCustomerId(cart.customerId());
        entity.setSessionId(cart.sessionId());
        entity.setStatus(cart.status());
        entity.setCreatedAt(cart.createdAt());
        entity.setUpdatedAt(cart.updatedAt());
        entity.setVersion(cart.version());

        // Totals
        CartTotals totals = cart.totals();
        entity.setSubtotalAmount(totals.subtotal().getAmount());
        entity.setSubtotalCurrency(totals.subtotal().getCurrency().name());
        entity.setDiscountAmount(totals.discount().getAmount());
        entity.setTaxAmount(totals.tax().getAmount());
        entity.setTotalAmount(totals.total().getAmount());

        // Promo code
        AppliedPromoCode promo = cart.appliedPromoCode();
        if (promo != null) {
            entity.setAppliedPromoCode(promo.code());
            entity.setPromoDiscountAmount(promo.discountAmount().getAmount());
            entity.setPromoDiscountCurrency(promo.discountAmount().getCurrency().name());
        } else {
            entity.setAppliedPromoCode(null);
            entity.setPromoDiscountAmount(null);
            entity.setPromoDiscountCurrency(null);
        }

        // Items
        entity.getItems().clear();
        for (CartItem item : cart.items()) {
            CartItemJpaEntity itemEntity = toItemEntity(item, objectMapper);
            itemEntity.setCart(entity);
            entity.getItems().add(itemEntity);
        }
    }

    default CartItemJpaEntity toItemEntity(CartItem item, ObjectMapper objectMapper) {
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setItemId(item.itemId().value());
        entity.setProductTemplateId(item.productTemplateId());
        entity.setProductName(item.productName());
        entity.setProductFamily(item.productFamily());
        entity.setThumbnailUrl(item.thumbnailUrl());

        try {
            entity.setConfigurationSnapshot(objectMapper.writeValueAsString(item.configurationSnapshot()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize configuration snapshot", e);
        }

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

    default Cart toDomain(CartJpaEntity entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        List<CartItem> items = entity.getItems().stream()
            .map(itemEntity -> toItemDomain(itemEntity, objectMapper))
            .toList();

        Currency currency = Currency.valueOf(entity.getSubtotalCurrency());
        CartTotals totals = new CartTotals(
            Money.of(entity.getSubtotalAmount(), currency),
            Money.of(entity.getDiscountAmount(), currency),
            Money.of(entity.getTaxAmount(), currency),
            Money.of(entity.getTotalAmount(), currency),
            items.stream().mapToInt(CartItem::quantity).sum()
        );

        AppliedPromoCode promo = null;
        if (entity.getAppliedPromoCode() != null) {
            Currency promoCurrency = entity.getPromoDiscountCurrency() != null
                ? Currency.valueOf(entity.getPromoDiscountCurrency())
                : currency;
            promo = new AppliedPromoCode(
                entity.getAppliedPromoCode(),
                Money.of(entity.getPromoDiscountAmount(), promoCurrency),
                null,
                null
            );
        }

        return Cart.rehydrate(
            CartId.of(entity.getCartId()),
            entity.getCustomerId(),
            entity.getSessionId(),
            entity.getStatus(),
            promo,
            totals,
            items,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }

    default CartItem toItemDomain(CartItemJpaEntity entity, ObjectMapper objectMapper) {
        ConfigurationSnapshot snapshot;
        try {
            snapshot = objectMapper.readValue(entity.getConfigurationSnapshot(), ConfigurationSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize configuration snapshot", e);
        }

        Currency currency = Currency.valueOf(entity.getUnitPriceCurrency());
        return CartItem.rehydrate(
            CartItemId.of(entity.getItemId()),
            entity.getProductTemplateId(),
            entity.getProductName(),
            entity.getProductFamily(),
            snapshot,
            entity.getConfigurationHash(),
            entity.getQuantity(),
            Money.of(entity.getUnitPriceAmount(), currency),
            new PriceQuoteReference(entity.getQuoteId(), entity.getQuoteValidUntil()),
            entity.getValidationStatus(),
            entity.getValidationMessage(),
            entity.getThumbnailUrl(),
            false,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
