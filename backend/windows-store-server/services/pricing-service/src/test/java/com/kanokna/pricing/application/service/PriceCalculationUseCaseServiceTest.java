package com.kanokna.pricing.application.service;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing.application.dto.QuoteResponse;
import com.kanokna.pricing.application.dto.ValidatePromoCodeCommand;
import com.kanokna.pricing.application.port.out.*;
import com.kanokna.pricing.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing.domain.exception.TaxRuleNotFoundException;
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
import java.util.UUID;

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

    @Test
    @DisplayName("Calculate quote saves result to cache and publishes event")
    void calculateQuote_SavesCacheAndPublishesEvent() {
        CalculateQuoteCommand command = command("PROMO10");
        PriceBook priceBook = priceBook();
        PromoCode promoCode = promoCode("PROMO10");
        TaxRule taxRule = taxRule();
        Quote calculatedQuote = calculatedQuote();

        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.of(priceBook));
        when(quoteCache.get(priceBook, command)).thenReturn(Optional.empty());
        when(campaignRepository.findActiveForProduct(command.getProductTemplateId()))
            .thenReturn(List.of());
        when(promoCodeRepository.findByCode(command.getPromoCode()))
            .thenReturn(Optional.of(promoCode));
        when(taxRuleRepository.findByRegion(command.getRegion())).thenReturn(Optional.of(taxRule));
        when(priceCalculationService.calculateQuote(
            eq(priceBook),
            eq(command.getResolvedBom()),
            eq(command.getWidthCm()),
            eq(command.getHeightCm()),
            anyList(),
            eq(promoCode),
            eq(taxRule),
            eq(5)
        )).thenReturn(calculatedQuote);

        QuoteResponse response = service.calculateQuote(command);

        assertEquals(calculatedQuote.getQuoteId().toString(), response.getQuoteId());
        assertEquals("WINDOW-STD", response.getProductTemplateId());
        verify(quoteCache).put(priceBook, command, calculatedQuote, 5);
        verify(eventPublisher).publishQuoteCalculated(any(QuoteCalculatedEvent.class));
        verify(promoCodeRepository).save(promoCode);
        assertEquals(1, promoCode.getUsageCount());
    }

    @Test
    @DisplayName("Expired cached quote triggers recalculation")
    void expiredCachedQuote_TriggersRecalculation() {
        CalculateQuoteCommand command = command(null);
        PriceBook priceBook = priceBook();
        Quote expiredQuote = expiredQuote();
        TaxRule taxRule = taxRule();
        Quote calculatedQuote = calculatedQuote();

        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.of(priceBook));
        when(quoteCache.get(priceBook, command)).thenReturn(Optional.of(expiredQuote));
        when(campaignRepository.findActiveForProduct(command.getProductTemplateId()))
            .thenReturn(List.of());
        when(taxRuleRepository.findByRegion(command.getRegion())).thenReturn(Optional.of(taxRule));
        when(priceCalculationService.calculateQuote(
            eq(priceBook),
            eq(command.getResolvedBom()),
            eq(command.getWidthCm()),
            eq(command.getHeightCm()),
            anyList(),
            isNull(),
            eq(taxRule),
            eq(5)
        )).thenReturn(calculatedQuote);

        QuoteResponse response = service.calculateQuote(command);

        assertEquals(calculatedQuote.getQuoteId().toString(), response.getQuoteId());
        verify(priceCalculationService).calculateQuote(
            eq(priceBook),
            eq(command.getResolvedBom()),
            eq(command.getWidthCm()),
            eq(command.getHeightCm()),
            anyList(),
            isNull(),
            eq(taxRule),
            eq(5)
        );
        verify(quoteCache).put(priceBook, command, calculatedQuote, 5);
    }

    @Test
    @DisplayName("Missing tax rule returns ERR-PRC-NO-TAXRULE")
    void missingTaxRuleThrows() {
        CalculateQuoteCommand command = command(null);
        PriceBook priceBook = priceBook();

        when(priceBookRepository.findActiveByProductTemplateId(command.getProductTemplateId()))
            .thenReturn(Optional.of(priceBook));
        when(quoteCache.get(priceBook, command)).thenReturn(Optional.empty());
        when(campaignRepository.findActiveForProduct(command.getProductTemplateId()))
            .thenReturn(List.of());
        when(taxRuleRepository.findByRegion(command.getRegion())).thenReturn(Optional.empty());

        assertThrows(TaxRuleNotFoundException.class, () -> service.calculateQuote(command));
        verify(priceCalculationService, never()).calculateQuote(any(), any(), any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("validatePromoCode returns ERR-PROMO-NOT-FOUND when code is missing")
    void validatePromoCode_NotFound() {
        ValidatePromoCodeCommand command = promoValidationCommand("MISSING", new BigDecimal("1000"));

        when(promoCodeRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        PromoCodeValidationResponse response = service.validatePromoCode(command);

        assertFalse(response.isValid());
        assertEquals("ERR-PROMO-NOT-FOUND", response.getErrorMessage());
        assertNull(response.getDiscountAmount());
    }

    @Test
    @DisplayName("validatePromoCode returns discount for valid fixed promo")
    void validatePromoCode_ValidPromo() {
        ValidatePromoCodeCommand command = promoValidationCommand("PROMO150", new BigDecimal("1000"));
        PromoCode promoCode = promoCode("PROMO150");

        when(promoCodeRepository.findByCode("PROMO150")).thenReturn(Optional.of(promoCode));

        PromoCodeValidationResponse response = service.validatePromoCode(command);

        assertTrue(response.isValid());
        assertNull(response.getErrorMessage());
        assertEquals("150 RUB", response.getDiscountAmount());
    }

    @Test
    @DisplayName("validatePromoCode returns ERR-PROMO-EXPIRED for expired promo")
    void validatePromoCode_ExpiredPromo() {
        ValidatePromoCodeCommand command = promoValidationCommand("EXPIRED", new BigDecimal("1000"));
        Instant now = Instant.now();
        PromoCode promoCode = PromoCode.restore(
            PromoCodeId.generate(),
            "EXPIRED",
            "Expired promo",
            DiscountType.FIXED,
            new BigDecimal("150"),
            null,
            null,
            10,
            0,
            now.minusSeconds(7200),
            now.minusSeconds(60),
            true,
            now.minusSeconds(10800),
            "tester"
        );

        when(promoCodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(promoCode));

        PromoCodeValidationResponse response = service.validatePromoCode(command);

        assertFalse(response.isValid());
        assertEquals("ERR-PROMO-EXPIRED", response.getErrorMessage());
    }

    @Test
    @DisplayName("validatePromoCode returns ERR-PROMO-EXHAUSTED when usage limit reached")
    void validatePromoCode_ExhaustedPromo() {
        ValidatePromoCodeCommand command = promoValidationCommand("EXHAUSTED", new BigDecimal("1000"));
        Instant now = Instant.now();
        PromoCode promoCode = PromoCode.restore(
            PromoCodeId.generate(),
            "EXHAUSTED",
            "Exhausted promo",
            DiscountType.FIXED,
            new BigDecimal("150"),
            null,
            null,
            1,
            1,
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now.minusSeconds(7200),
            "tester"
        );

        when(promoCodeRepository.findByCode("EXHAUSTED")).thenReturn(Optional.of(promoCode));

        PromoCodeValidationResponse response = service.validatePromoCode(command);

        assertFalse(response.isValid());
        assertEquals("ERR-PROMO-EXHAUSTED", response.getErrorMessage());
    }

    @Test
    @DisplayName("validatePromoCode returns ERR-PROMO-MIN-SUBTOTAL when subtotal is too low")
    void validatePromoCode_MinSubtotalNotReached() {
        ValidatePromoCodeCommand command = promoValidationCommand("MIN-SUBTOTAL", new BigDecimal("400"));
        Instant now = Instant.now();
        PromoCode promoCode = PromoCode.restore(
            PromoCodeId.generate(),
            "MIN-SUBTOTAL",
            "Min subtotal promo",
            DiscountType.FIXED,
            new BigDecimal("100"),
            null,
            Money.of(new BigDecimal("500"), "RUB"),
            10,
            0,
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now.minusSeconds(7200),
            "tester"
        );

        when(promoCodeRepository.findByCode("MIN-SUBTOTAL")).thenReturn(Optional.of(promoCode));

        PromoCodeValidationResponse response = service.validatePromoCode(command);

        assertFalse(response.isValid());
        assertEquals("ERR-PROMO-MIN-SUBTOTAL", response.getErrorMessage());
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

    private ValidatePromoCodeCommand promoValidationCommand(String promoCode, BigDecimal subtotal) {
        ValidatePromoCodeCommand command = new ValidatePromoCodeCommand();
        command.setPromoCode(promoCode);
        command.setSubtotal(subtotal);
        command.setCurrency("RUB");
        return command;
    }

    private PriceBook priceBook() {
        BasePriceEntry basePriceEntry = BasePriceEntry.of("WINDOW-STD", new BigDecimal("1000"), new BigDecimal("0.25"), null);
        PriceBook priceBook = PriceBook.create(PriceBookId.generate(), "WINDOW-STD", "RUB", basePriceEntry, "tester");
        priceBook.publish();
        return priceBook;
    }

    private TaxRule taxRule() {
        return TaxRule.createVAT(TaxRuleId.generate(), "RU", "Russia", new BigDecimal("20"));
    }

    private PromoCode promoCode(String code) {
        Instant now = Instant.now();
        return PromoCode.restore(
            PromoCodeId.generate(),
            code,
            "Fixed discount promo",
            DiscountType.FIXED,
            new BigDecimal("150"),
            null,
            null,
            10,
            0,
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now.minusSeconds(7200),
            "tester"
        );
    }

    private Quote calculatedQuote() {
        return Quote.builder()
            .quoteId(QuoteId.of(UUID.fromString("11111111-1111-1111-1111-111111111111")))
            .productTemplateId("WINDOW-STD")
            .basePrice(Money.of(new BigDecimal("1000"), "RUB"))
            .optionPremiums(List.of())
            .discount(Money.of(new BigDecimal("150"), "RUB"))
            .subtotal(Money.of(new BigDecimal("850"), "RUB"))
            .tax(Money.of(new BigDecimal("170"), "RUB"))
            .total(Money.of(new BigDecimal("1020"), "RUB"))
            .validUntil(Instant.now().plusSeconds(300))
            .decisionTrace(List.of(PricingDecision.of("BASE", "BASE_PRICE", "1000 RUB")))
            .build();
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

    private Quote expiredQuote() {
        return Quote.builder()
            .quoteId(QuoteId.generate())
            .productTemplateId("WINDOW-STD")
            .basePrice(Money.of(new BigDecimal("1000"), "RUB"))
            .optionPremiums(List.of())
            .discount(Money.of(BigDecimal.ZERO, "RUB"))
            .subtotal(Money.of(new BigDecimal("1000"), "RUB"))
            .tax(Money.of(BigDecimal.ZERO, "RUB"))
            .total(Money.of(new BigDecimal("1000"), "RUB"))
            .validUntil(Instant.now().minusSeconds(1))
            .decisionTrace(List.of())
            .build();
    }
}
