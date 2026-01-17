package com.kanokna.pricing.application.service;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.application.dto.QuoteResponse;
import com.kanokna.pricing.application.port.out.*;
import com.kanokna.pricing.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing.domain.model.*;
import com.kanokna.pricing.domain.service.PriceCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceCalculationUseCaseServiceTest {

    @Mock
    private PriceBookRepository priceBookRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private TaxRuleRepository taxRuleRepository;
    @Mock
    private QuoteCache quoteCache;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private PriceCalculationService priceCalculationService;

    private PriceCalculationUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new PriceCalculationUseCaseService(
            priceBookRepository,
            campaignRepository,
            promoCodeRepository,
            taxRuleRepository,
            quoteCache,
            eventPublisher,
            priceCalculationService,
            5
        );
    }

    @Test
    @DisplayName("TC-PRC-008: Missing price book returns ERR-PRC-NO-PRICEBOOK")
    void missingPriceBookThrows() {
        CalculateQuoteCommand command = command(null);
        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.empty());

        assertThrows(PriceBookNotFoundException.class, () -> service.calculateQuote(command));
    }

    @Test
    @DisplayName("TC-PRC-009: Invalid promo code returns ERR-PRC-INVALID-PROMO")
    void invalidPromoThrows() {
        CalculateQuoteCommand command = command("BADCODE");
        PriceBook priceBook = priceBook();

        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.of(priceBook));
        when(quoteCache.get(priceBook, command)).thenReturn(Optional.empty());
        when(campaignRepository.findActiveForProduct(command.getProductTemplateId()))
            .thenReturn(List.of());
        when(promoCodeRepository.findByCode(command.getPromoCode())).thenReturn(Optional.empty());

        assertThrows(InvalidPromoCodeException.class, () -> service.calculateQuote(command));
    }

    @Test
    @DisplayName("TC-PRC-011 / TC-FUNC-CALC-012: Cached quote returned for identical inputs")
    void cachedQuoteReturned() {
        CalculateQuoteCommand command = command(null);
        PriceBook priceBook = priceBook();
        Quote cachedQuote = cachedQuote();

        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.of(priceBook));
        when(quoteCache.get(priceBook, command)).thenReturn(Optional.of(cachedQuote));

        QuoteResponse response = service.calculateQuote(command);

        assertEquals(cachedQuote.getQuoteId().toString(), response.getQuoteId());
        verify(priceCalculationService, never()).calculateQuote(any(), any(), any(), any(), any(), any(), any(), anyInt());
        verify(eventPublisher, never()).publishQuoteCalculated(any(QuoteCalculatedEvent.class));
    }

    private CalculateQuoteCommand command(String promoCode) {
        CalculateQuoteCommand command = new CalculateQuoteCommand();
        command.setProductTemplateId("WINDOW-STD");
        command.setWidthCm(new BigDecimal("100"));
        command.setHeightCm(new BigDecimal("100"));
        command.setResolvedBom(List.of("OPT-A"));
        command.setCurrency("RUB");
        command.setPromoCode(promoCode);
        command.setRegion("RU");
        return command;
    }

    private PriceBook priceBook() {
        BasePriceEntry basePriceEntry = BasePriceEntry.of("WINDOW-STD", new BigDecimal("1000"), new BigDecimal("0.25"), null);
        PriceBook priceBook = PriceBook.create(PriceBookId.generate(), "WINDOW-STD", "RUB", basePriceEntry, "tester");
        priceBook.publish();
        return priceBook;
    }

    private Quote cachedQuote() {
        return Quote.builder()
            .quoteId(QuoteId.generate())
            .productTemplateId("WINDOW-STD")
            .basePrice(Money.of(new BigDecimal("1000"), "RUB"))
            .optionPremiums(List.of())
            .discount(Money.of(BigDecimal.ZERO, "RUB"))
            .subtotal(Money.of(new BigDecimal("1000"), "RUB"))
            .tax(Money.of(BigDecimal.ZERO, "RUB"))
            .total(Money.of(new BigDecimal("1000"), "RUB"))
            .validUntil(Instant.now().plusSeconds(300))
            .decisionTrace(List.of())
            .build();
    }
}
