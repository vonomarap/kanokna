package com.kanokna.cart.application.port.out;

import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.shared.money.Money;
import java.time.Instant;
import java.util.List;

/**
 * Outbound port for pricing operations.
 */
public interface PricingClient {
    PriceQuote calculateQuote(PriceQuoteRequest request);

    PromoValidationResult validatePromoCode(PromoValidationRequest request);

    record PriceQuoteRequest(
        String productTemplateId,
        DimensionsDto dimensions,
        List<BomLineDto> resolvedBom,
        String currency,
        String promoCode,
        String region
    ) {
    }

    record PriceQuote(
        boolean available,
        String quoteId,
        Money unitPrice,
        Instant validUntil
    ) {
    }

    record PromoValidationRequest(
        String promoCode,
        Money subtotal
    ) {
    }

    record PromoValidationResult(
        boolean available,
        boolean valid,
        Money discountAmount,
        String errorMessage,
        String errorCode
    ) {
    }
}
