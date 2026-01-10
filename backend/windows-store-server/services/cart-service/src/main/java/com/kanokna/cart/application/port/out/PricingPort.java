package com.kanokna.cart.application.port.out;

import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.time.Instant;

/**
 * Outbound port for pricing-service integration.
 */
public interface PricingPort {
    PriceQuote calculateQuote(ConfigurationSnapshot snapshot, Currency currency);

    PromoValidationResult validatePromoCode(String promoCode, Money subtotal);

    record PriceQuote(
        boolean available,
        String quoteId,
        Money unitPrice,
        Instant validUntil
    ) {
        public static PriceQuote unavailable() {
            return new PriceQuote(false, null, null, null);
        }
    }

    record PromoValidationResult(
        boolean available,
        boolean valid,
        Money discountAmount,
        String errorCode,
        String errorMessage
    ) {
        public static PromoValidationResult unavailable() {
            return new PromoValidationResult(false, false, null, null, null);
        }
    }
}
