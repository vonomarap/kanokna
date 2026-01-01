package com.kanokna.pricing_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteTest {

    @Test
    @DisplayName("Quote isExpired returns false for future validUntil")
    void isExpiredReturnsFalse() {
        Quote quote = quoteWithValidUntil(Instant.now().plusSeconds(60));
        assertFalse(quote.isExpired());
    }

    @Test
    @DisplayName("Quote isExpired returns true for past validUntil")
    void isExpiredReturnsTrue() {
        Quote quote = quoteWithValidUntil(Instant.now().minusSeconds(60));
        assertTrue(quote.isExpired());
    }

    private Quote quoteWithValidUntil(Instant validUntil) {
        return Quote.builder()
            .quoteId(QuoteId.generate())
            .productTemplateId("WINDOW-STD")
            .basePrice(Money.of(new BigDecimal("100"), "RUB"))
            .optionPremiums(List.of())
            .discount(Money.of(new BigDecimal("0"), "RUB"))
            .subtotal(Money.of(new BigDecimal("100"), "RUB"))
            .tax(Money.of(new BigDecimal("0"), "RUB"))
            .total(Money.of(new BigDecimal("100"), "RUB"))
            .validUntil(validUntil)
            .decisionTrace(List.of())
            .build();
    }
}
