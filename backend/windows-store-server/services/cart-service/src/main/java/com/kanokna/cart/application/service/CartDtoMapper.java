package com.kanokna.cart.application.service;

import com.kanokna.cart.application.dto.*;
import com.kanokna.cart.domain.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting domain models to DTOs.
 * Consolidates all Cart-related DTO mapping logic.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.items().stream()
            .map(CartDtoMapper::toDto)
            .toList();
        return toDto(cart, items);
    }

    public static CartDto toDto(Cart cart, CartItemValidationService.RevalidationSummary revalidation) {
        Instant now = Instant.now();
        List<CartItemDto> items = new ArrayList<>();

        for (int i = 0; i < cart.items().size(); i++) {
            CartItem item = cart.items().get(i);
            CartItemValidationService.ItemValidationResult valResult = null;
            if (revalidation != null && i < revalidation.results().size()) {
                valResult = revalidation.results().get(i);
            }

            ValidationStatus status = valResult != null ? valResult.status() : item.validationStatus();
            String message = valResult != null ? valResult.message() : item.validationMessage();
            boolean priceStale = item.quoteReference().isStale(now);

            items.add(toDto(item, status, message, priceStale));
        }

        return toDto(cart, items);
    }

    public static CartDto toDto(Cart cart, List<CartItemDto> items) {
        return new CartDto(
            cart.cartId().toString(),
            cart.customerId(),
            cart.sessionId(),
            cart.status(),
            items,
            cart.totals().subtotal(),
            cart.totals().discount(),
            cart.totals().tax(),
            cart.totals().total(),
            toDto(cart.appliedPromoCode()),
            cart.totals().itemCount(),
            cart.createdAt(),
            cart.updatedAt()
        );
    }

    public static CartItemDto toDto(CartItem item) {
        return toDto(item, item.validationStatus(), item.validationMessage(), item.priceStale());
    }

    public static CartItemDto toDto(CartItem item, ValidationStatus status, String message, boolean priceStale) {
        ConfigurationSnapshot snapshot = item.configurationSnapshot();
        List<SelectedOptionDto> options = snapshot.selectedOptions().stream()
            .map(opt -> new SelectedOptionDto(opt.optionGroupId(), opt.optionId()))
            .toList();
        List<BomLineDto> bomLines = snapshot.resolvedBom().stream()
            .map(line -> new BomLineDto(line.sku(), line.description(), line.quantity()))
            .toList();

        return new CartItemDto(
            item.itemId().toString(),
            item.productTemplateId(),
            item.productName(),
            item.productFamily(),
            new DimensionsDto(snapshot.widthCm(), snapshot.heightCm()),
            options,
            bomLines,
            item.quantity(),
            item.unitPrice(),
            item.lineTotal(),
            item.quoteReference().quoteId(),
            item.quoteReference().validUntil(),
            status,
            message,
            priceStale,
            item.configurationHash(),
            item.thumbnailUrl()
        );
    }

    public static AppliedPromoCodeDto toDto(AppliedPromoCode promo) {
        if (promo == null) {
            return null;
        }
        return new AppliedPromoCodeDto(
            promo.code(),
            promo.discountAmount(),
            promo.description(),
            promo.appliedAt()
        );
    }

    public static CartSnapshotDto toSnapshotDto(CartSnapshot snapshot) {
        List<CartItemDto> items = snapshot.items().stream()
            .map(CartDtoMapper::toSnapshotItemDto)
            .toList();

        return new CartSnapshotDto(
            snapshot.snapshotId().toString(),
            snapshot.cartId().toString(),
            snapshot.customerId(),
            items,
            snapshot.totals().subtotal(),
            snapshot.totals().discount(),
            snapshot.totals().tax(),
            snapshot.totals().total(),
            toDto(snapshot.appliedPromoCode()),
            snapshot.totals().itemCount(),
            snapshot.createdAt(),
            snapshot.validUntil()
        );
    }

    private static CartItemDto toSnapshotItemDto(CartSnapshotItem item) {
        ConfigurationSnapshot snapshot = item.configurationSnapshot();
        List<SelectedOptionDto> options = snapshot.selectedOptions().stream()
            .map(opt -> new SelectedOptionDto(opt.optionGroupId(), opt.optionId()))
            .toList();
        List<BomLineDto> bomLines = snapshot.resolvedBom().stream()
            .map(line -> new BomLineDto(line.sku(), line.description(), line.quantity()))
            .toList();

        return new CartItemDto(
            item.itemId(),
            item.productTemplateId(),
            item.productName(),
            item.productFamily(),
            new DimensionsDto(snapshot.widthCm(), snapshot.heightCm()),
            options,
            bomLines,
            item.quantity(),
            item.unitPrice(),
            item.lineTotal(),
            item.quoteReference().quoteId(),
            item.quoteReference().validUntil(),
            ValidationStatus.VALID,
            null,
            false,
            item.configurationHash(),
            item.thumbnailUrl()
        );
    }
}
