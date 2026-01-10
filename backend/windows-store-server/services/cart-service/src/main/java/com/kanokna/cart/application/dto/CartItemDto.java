package com.kanokna.cart.application.dto;

import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.List;

/**
 * DTO representing a cart item for API responses.
 */
public record CartItemDto(
    String itemId,
    String productTemplateId,
    String productName,
    String productFamily,
    DimensionsDto dimensions,
    List<SelectedOptionDto> selectedOptions,
    List<BomLineDto> resolvedBom,
    int quantity,
    Money unitPrice,
    Money lineTotal,
    String quoteId,
    Instant quoteValidUntil,
    ValidationStatus validationStatus,
    String validationMessage,
    boolean priceStale,
    String configurationHash,
    String thumbnailUrl
) {
}
