package com.kanokna.cart.adapters.in.grpc;

import com.google.protobuf.Timestamp;
import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
import com.kanokna.cart.application.dto.AppliedPromoCodeDto;
import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.CartItemDto;
import com.kanokna.cart.application.dto.CartSnapshotDto;
import com.kanokna.cart.application.dto.ClearCartCommand;
import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.dto.GetCartQuery;
import com.kanokna.cart.application.dto.MergeCartsCommand;
import com.kanokna.cart.application.dto.MergeCartsResult;
import com.kanokna.cart.application.dto.RefreshPricesCommand;
import com.kanokna.cart.application.dto.RefreshPricesResult;
import com.kanokna.cart.application.dto.RemoveItemCommand;
import com.kanokna.cart.application.dto.RemovePromoCodeCommand;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import com.kanokna.cart.application.dto.UpdateItemCommand;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.v1.AddItemRequest;
import com.kanokna.cart.v1.AddItemResponse;
import com.kanokna.cart.v1.ApplyPromoCodeRequest;
import com.kanokna.cart.v1.ApplyPromoCodeResponse;
import com.kanokna.cart.v1.Cart;
import com.kanokna.cart.v1.CartItem;
import com.kanokna.cart.v1.ClearCartRequest;
import com.kanokna.cart.v1.ClearCartResponse;
import com.kanokna.cart.v1.CreateSnapshotRequest;
import com.kanokna.cart.v1.CreateSnapshotResponse;
import com.kanokna.cart.v1.GetCartRequest;
import com.kanokna.cart.v1.GetCartResponse;
import com.kanokna.cart.v1.MergeCartsRequest;
import com.kanokna.cart.v1.MergeCartsResponse;
import com.kanokna.cart.v1.RefreshPricesRequest;
import com.kanokna.cart.v1.RefreshPricesResponse;
import com.kanokna.cart.v1.RemoveItemRequest;
import com.kanokna.cart.v1.RemoveItemResponse;
import com.kanokna.cart.v1.RemovePromoCodeRequest;
import com.kanokna.cart.v1.RemovePromoCodeResponse;
import com.kanokna.cart.v1.UpdateItemRequest;
import com.kanokna.cart.v1.UpdateItemResponse;
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.catalog.v1.SelectedOption;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Dimensions;
import com.kanokna.common.v1.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CartGrpcMapper {
    public GetCartQuery toQuery(GetCartRequest request) {
        return new GetCartQuery(blankToNull(request.getCustomerId()), blankToNull(request.getSessionId()));
    }

    public AddItemCommand toCommand(AddItemRequest request) {
        return new AddItemCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId()),
            request.getProductTemplateId(),
            request.getProductName(),
            blankToNull(request.getProductFamily()),
            blankToNull(request.getThumbnailUrl()),
            toDto(request.getDimensions()),
            toSelectedOptionDtos(request.getSelectedOptionsList()),
            request.getQuantity(),
            blankToNull(request.getQuoteId()),
            toBomLineDtos(request.getResolvedBom())
        );
    }

    public UpdateItemCommand toCommand(UpdateItemRequest request) {
        return new UpdateItemCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId()),
            request.getItemId(),
            request.getQuantity()
        );
    }

    public RemoveItemCommand toCommand(RemoveItemRequest request) {
        return new RemoveItemCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId()),
            request.getItemId()
        );
    }

    public ClearCartCommand toCommand(ClearCartRequest request) {
        return new ClearCartCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId())
        );
    }

    public ApplyPromoCodeCommand toCommand(ApplyPromoCodeRequest request) {
        return new ApplyPromoCodeCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId()),
            request.getPromoCode()
        );
    }

    public RemovePromoCodeCommand toCommand(RemovePromoCodeRequest request) {
        return new RemovePromoCodeCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId())
        );
    }

    public MergeCartsCommand toCommand(MergeCartsRequest request) {
        return new MergeCartsCommand(
            request.getCustomerId(),
            request.getAnonymousSessionId()
        );
    }

    public RefreshPricesCommand toCommand(RefreshPricesRequest request) {
        return new RefreshPricesCommand(
            blankToNull(request.getCustomerId()),
            blankToNull(request.getSessionId())
        );
    }

    public CreateSnapshotCommand toCommand(CreateSnapshotRequest request) {
        return new CreateSnapshotCommand(
            request.getCustomerId(),
            request.getAcknowledgePriceChanges()
        );
    }

    public GetCartResponse toResponse(CartDto cart) {
        return GetCartResponse.newBuilder()
            .setCart(toCart(cart))
            .build();
    }

    public AddItemResponse toResponse(AddItemResult result) {
        return AddItemResponse.newBuilder()
            .setCart(toCart(result.cart()))
            .setAddedItemId(result.addedItemId())
            .build();
    }

    public UpdateItemResponse toResponse(CartDto cart) {
        return UpdateItemResponse.newBuilder()
            .setCart(toCart(cart))
            .build();
    }

    public RemoveItemResponse toResponseRemoved(CartDto cart) {
        return RemoveItemResponse.newBuilder()
            .setCart(toCart(cart))
            .build();
    }

    public ClearCartResponse toResponseCleared(CartDto cart) {
        return ClearCartResponse.newBuilder()
            .setCart(toCart(cart))
            .build();
    }

    public ApplyPromoCodeResponse toResponse(ApplyPromoCodeResult result) {
        ApplyPromoCodeResponse.Builder builder = ApplyPromoCodeResponse.newBuilder()
            .setCart(toCart(result.cart()))
            .setApplied(result.applied());
        if (result.errorMessage() != null) {
            builder.setErrorMessage(result.errorMessage());
        }
        if (result.errorCode() != null) {
            builder.setErrorCode(result.errorCode());
        }
        return builder.build();
    }

    public RemovePromoCodeResponse toResponsePromoRemoved(CartDto cart) {
        return RemovePromoCodeResponse.newBuilder()
            .setCart(toCart(cart))
            .build();
    }

    public MergeCartsResponse toResponse(MergeCartsResult result) {
        return MergeCartsResponse.newBuilder()
            .setMergedCart(toCart(result.mergedCart()))
            .setItemsFromAnonymous(result.itemsFromAnonymous())
            .setItemsMerged(result.itemsMerged())
            .setItemsAdded(result.itemsAdded())
            .setPromoCodePreserved(result.promoCodePreserved())
            .setPromoCodeSource(result.promoCodeSource() == null ? "" : result.promoCodeSource())
            .build();
    }

    public RefreshPricesResponse toResponse(RefreshPricesResult result) {
        RefreshPricesResponse.Builder builder = RefreshPricesResponse.newBuilder()
            .setCart(toCart(result.cart()))
            .setItemsUpdated(result.itemsUpdated())
            .setTotalChanged(result.totalChanged())
            .setPriceChangePercent(result.priceChangePercent());
        if (result.previousTotal() != null) {
            builder.setPreviousTotal(toMoney(result.previousTotal()));
        }
        return builder.build();
    }

    public CreateSnapshotResponse toResponse(CreateSnapshotResult result) {
        CreateSnapshotResponse.Builder builder = CreateSnapshotResponse.newBuilder()
            .setSnapshotId(result.snapshotId())
            .setCartSnapshot(toSnapshotCart(result.cartSnapshot()))
            .setPricesChanged(result.pricesChanged());
        if (result.validUntil() != null) {
            builder.setValidUntil(toTimestamp(result.validUntil()));
        }
        if (result.previousTotal() != null) {
            builder.setPreviousTotal(toMoney(result.previousTotal()));
        }
        return builder.build();
    }

    private Cart toSnapshotCart(CartSnapshotDto snapshot) {
        if (snapshot == null) {
            return Cart.newBuilder().build();
        }
        Cart.Builder builder = Cart.newBuilder()
            .setCartId(snapshot.cartId())
            .setCustomerId(snapshot.customerId())
            .setStatus(com.kanokna.cart.v1.CartStatus.CART_STATUS_UNSPECIFIED)
            .setItemCount(snapshot.itemCount())
            .setSubtotal(toMoney(snapshot.subtotal()))
            .setDiscount(toMoney(snapshot.discount()))
            .setTax(toMoney(snapshot.tax()))
            .setTotal(toMoney(snapshot.total()));
        if (snapshot.appliedPromoCode() != null) {
            builder.setAppliedPromoCode(toAppliedPromo(snapshot.appliedPromoCode()));
        }
        if (snapshot.createdAt() != null) {
            builder.setCreatedAt(toTimestamp(snapshot.createdAt()));
            builder.setUpdatedAt(toTimestamp(snapshot.createdAt()));
        }
        if (snapshot.items() != null) {
            snapshot.items().forEach(item -> builder.addItems(toCartItem(item)));
        }
        return builder.build();
    }

    private Cart toCart(CartDto cart) {
        Cart.Builder builder = Cart.newBuilder()
            .setCartId(cart.cartId())
            .setCustomerId(cart.customerId() == null ? "" : cart.customerId())
            .setSessionId(cart.sessionId() == null ? "" : cart.sessionId())
            .setStatus(mapStatus(cart.status()))
            .setSubtotal(toMoney(cart.subtotal()))
            .setDiscount(toMoney(cart.discount()))
            .setTax(toMoney(cart.tax()))
            .setTotal(toMoney(cart.total()))
            .setItemCount(cart.itemCount());

        if (cart.updatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(cart.updatedAt()));
        }
        if (cart.createdAt() != null) {
            builder.setCreatedAt(toTimestamp(cart.createdAt()));
        }
        if (cart.appliedPromoCode() != null) {
            builder.setAppliedPromoCode(toAppliedPromo(cart.appliedPromoCode()));
        }
        if (cart.items() != null) {
            cart.items().forEach(item -> builder.addItems(toCartItem(item)));
        }
        return builder.build();
    }

    private CartItem toCartItem(CartItemDto item) {
        CartItem.Builder builder = CartItem.newBuilder()
            .setItemId(item.itemId())
            .setProductTemplateId(item.productTemplateId())
            .setProductName(item.productName())
            .setProductFamily(item.productFamily() == null ? "" : item.productFamily())
            .setDimensions(toDimensions(item.dimensions()))
            .setQuantity(item.quantity())
            .setUnitPrice(toMoney(item.unitPrice()))
            .setLineTotal(toMoney(item.lineTotal()))
            .setQuoteId(item.quoteId() == null ? "" : item.quoteId())
            .setValidationStatus(mapValidationStatus(item.validationStatus()))
            .setValidationMessage(item.validationMessage() == null ? "" : item.validationMessage())
            .setPriceStale(item.priceStale())
            .setConfigurationHash(item.configurationHash() == null ? "" : item.configurationHash())
            .setThumbnailUrl(item.thumbnailUrl() == null ? "" : item.thumbnailUrl());

        if (item.quoteValidUntil() != null) {
            builder.setQuoteValidUntil(toTimestamp(item.quoteValidUntil()));
        }
        if (item.selectedOptions() != null) {
            item.selectedOptions().forEach(option -> builder.addSelectedOptions(toSelectedOption(option)));
        }
        if (item.resolvedBom() != null && !item.resolvedBom().isEmpty()) {
            builder.setResolvedBom(toBillOfMaterials(item.resolvedBom()));
        }
        return builder.build();
    }

    private com.kanokna.cart.v1.AppliedPromoCode toAppliedPromo(AppliedPromoCodeDto promo) {
        com.kanokna.cart.v1.AppliedPromoCode.Builder builder =
            com.kanokna.cart.v1.AppliedPromoCode.newBuilder()
                .setCode(promo.code())
                .setDiscountAmount(toMoney(promo.discountAmount()));
        if (promo.description() != null) {
            builder.setDescription(promo.description());
        }
        if (promo.appliedAt() != null) {
            builder.setAppliedAt(toTimestamp(promo.appliedAt()));
        }
        return builder.build();
    }

    private List<SelectedOptionDto> toSelectedOptionDtos(List<SelectedOption> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
            .map(option -> new SelectedOptionDto(option.getOptionGroupId(), option.getOptionId()))
            .toList();
    }

    private List<BomLineDto> toBomLineDtos(BillOfMaterials bom) {
        if (bom == null || bom.getLinesCount() == 0) {
            return List.of();
        }
        return bom.getLinesList().stream()
            .map(line -> new BomLineDto(line.getSku(), line.getDescription(), line.getQuantity()))
            .toList();
    }

    private SelectedOption toSelectedOption(SelectedOptionDto dto) {
        return SelectedOption.newBuilder()
            .setOptionGroupId(dto.optionGroupId())
            .setOptionId(dto.optionId())
            .build();
    }

    private BillOfMaterials toBillOfMaterials(List<BomLineDto> lines) {
        BillOfMaterials.Builder builder = BillOfMaterials.newBuilder();
        lines.forEach(line -> builder.addLines(toBomLine(line)));
        return builder.build();
    }

    private BomLine toBomLine(BomLineDto line) {
        return BomLine.newBuilder()
            .setSku(line.sku())
            .setDescription(line.description())
            .setQuantity(line.quantity())
            .build();
    }

    private Dimensions toDimensions(DimensionsDto dto) {
        if (dto == null) {
            return Dimensions.newBuilder().build();
        }
        return Dimensions.newBuilder()
            .setWidthCm(dto.widthCm())
            .setHeightCm(dto.heightCm())
            .build();
    }

    private DimensionsDto toDto(Dimensions dimensions) {
        return new DimensionsDto(dimensions.getWidthCm(), dimensions.getHeightCm());
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

    private com.kanokna.cart.v1.CartStatus mapStatus(CartStatus status) {
        if (status == null) {
            return com.kanokna.cart.v1.CartStatus.CART_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case ACTIVE -> com.kanokna.cart.v1.CartStatus.CART_STATUS_ACTIVE;
            case CHECKED_OUT -> com.kanokna.cart.v1.CartStatus.CART_STATUS_CHECKED_OUT;
            case ABANDONED -> com.kanokna.cart.v1.CartStatus.CART_STATUS_ABANDONED;
            case MERGED -> com.kanokna.cart.v1.CartStatus.CART_STATUS_MERGED;
        };
    }

    private com.kanokna.cart.v1.ValidationStatus mapValidationStatus(ValidationStatus status) {
        if (status == null) {
            return com.kanokna.cart.v1.ValidationStatus.VALIDATION_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case VALID -> com.kanokna.cart.v1.ValidationStatus.VALIDATION_STATUS_VALID;
            case INVALID -> com.kanokna.cart.v1.ValidationStatus.VALIDATION_STATUS_INVALID;
            case UNKNOWN -> com.kanokna.cart.v1.ValidationStatus.VALIDATION_STATUS_UNKNOWN;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
