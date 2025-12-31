package com.kanokna.pricing.application.service;

import com.kanokna.pricing.application.dto.*;
import com.kanokna.pricing.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing.application.port.in.ValidatePromoCodeUseCase;
import com.kanokna.pricing.application.port.out.*;
import com.kanokna.pricing.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing.domain.exception.TaxRuleNotFoundException;
import com.kanokna.pricing.domain.model.*;
import com.kanokna.pricing.domain.service.DiscountService;
import com.kanokna.pricing.domain.service.PriceCalculationService;
import com.kanokna.pricing.domain.service.RoundingService;
import com.kanokna.pricing.domain.service.TaxCalculationService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/* <FUNCTION_CONTRACT id="FC-pricing-service-UC-PRICING-QUOTE-validatePromoCode"
     LAYER="application.service"
     INTENT="Validate a promotional code and calculate potential discount"
     INPUT="ValidatePromoCodeCommand (promoCode, subtotal)"
     OUTPUT="PromoCodeValidationResponse"
     SIDE_EFFECTS="None (validation only)"
     LINKS="RequirementsAnalysis.xml#UC-PRICING-QUOTE">
  <PRECONDITIONS>
    <Item>command.promoCode is not blank</Item>
    <Item>command.subtotal is positive</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>Response.valid is true iff code exists, not expired, and has remaining uses</Item>
    <Item>Response.discountAmount is calculated if valid</Item>
    <Item>Response.errorMessage explains invalidity if not valid</Item>
  </POSTCONDITIONS>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="ERR-PROMO-NOT-FOUND">Promo code does not exist</Item>
    <Item type="BUSINESS" code="ERR-PROMO-EXPIRED">Promo code has expired</Item>
    <Item type="BUSINESS" code="ERR-PROMO-EXHAUSTED">Promo code usage limit reached</Item>
    <Item type="BUSINESS" code="ERR-PROMO-MIN-SUBTOTAL">Subtotal below minimum for promo</Item>
  </POSTCONDITIONS>

  <BLOCK_ANCHORS>
    <Item id="BA-PROMO-VAL-01">Load promo code</Item>
    <Item id="BA-PROMO-VAL-02">Check expiry</Item>
    <Item id="BA-PROMO-VAL-03">Check usage limits</Item>
    <Item id="BA-PROMO-VAL-04">Calculate discount amount</Item>
  </BLOCK_ANCHORS>

  <TESTS>
    <Case id="TC-PROMO-001">Valid promo code returns valid=true with discount amount</Case>
    <Case id="TC-PROMO-002">Non-existent code returns valid=false with ERR-PROMO-NOT-FOUND</Case>
    <Case id="TC-PROMO-003">Expired code returns valid=false with ERR-PROMO-EXPIRED</Case>
    <Case id="TC-PROMO-004">Exhausted code returns valid=false with ERR-PROMO-EXHAUSTED</Case>
    <Case id="TC-PROMO-005">Subtotal below minimum returns valid=false with ERR-PROMO-MIN-SUBTOTAL</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

/**
 * Application service implementing price calculation use cases.
 * Coordinates domain services with infrastructure via ports.
 */
public class PriceCalculationUseCaseService implements CalculateQuoteUseCase, ValidatePromoCodeUseCase {

    private final PriceBookRepository priceBookRepository;
    private final CampaignRepository campaignRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final TaxRuleRepository taxRuleRepository;
    private final QuoteCache quoteCache;
    private final EventPublisher eventPublisher;
    private final PriceCalculationService priceCalculationService;
    private final int quoteTtlMinutes;

    public PriceCalculationUseCaseService(
            PriceBookRepository priceBookRepository,
            CampaignRepository campaignRepository,
            PromoCodeRepository promoCodeRepository,
            TaxRuleRepository taxRuleRepository,
            QuoteCache quoteCache,
            EventPublisher eventPublisher,
            int quoteTtlMinutes) {
        this.priceBookRepository = priceBookRepository;
        this.campaignRepository = campaignRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.taxRuleRepository = taxRuleRepository;
        this.quoteCache = quoteCache;
        this.eventPublisher = eventPublisher;
        this.quoteTtlMinutes = quoteTtlMinutes;

        // Initialize domain services
        this.priceCalculationService = new PriceCalculationService(
            new DiscountService(),
            new TaxCalculationService(),
            new RoundingService()
        );
    }

    @Override
    public QuoteResponse calculateQuote(CalculateQuoteCommand command) {
        // BA-PRC-CALC-01: Load active price book
        PriceBook priceBook = priceBookRepository
            .findActiveByProductTemplateId(command.getProductTemplateId())
            .orElseThrow(() -> new PriceBookNotFoundException(command.getProductTemplateId()));

        // Load active campaigns for product
        List<Campaign> campaigns = campaignRepository
            .findActiveForProduct(command.getProductTemplateId());

        // Load promo code if provided
        PromoCode promoCode = null;
        if (command.getPromoCode() != null && !command.getPromoCode().isBlank()) {
            promoCode = promoCodeRepository.findByCode(command.getPromoCode()).orElse(null);
        }

        // Load tax rule
        TaxRule taxRule = taxRuleRepository.findByRegion(command.getRegion())
            .orElseThrow(() -> new TaxRuleNotFoundException(command.getRegion()));

        // Calculate quote using domain service
        Quote quote = priceCalculationService.calculateQuote(
            priceBook,
            command.getSelectedOptionIds(),
            command.getWidthCm(),
            command.getHeightCm(),
            campaigns,
            promoCode,
            taxRule,
            quoteTtlMinutes
        );

        // Cache quote
        quoteCache.put(quote.getQuoteId(), quote, quoteTtlMinutes);

        // Publish event
        eventPublisher.publish("pricing.quote.calculated",
            com.kanokna.pricing.domain.event.QuoteCalculatedEvent.of(
                quote.getQuoteId(),
                quote.getProductTemplateId(),
                quote.getTotal().getCurrency(),
                quote.getTotal().toString()
            ));

        // Map to response DTO
        return mapToResponse(quote);
    }

    @Override
    public PromoCodeValidationResponse validatePromoCode(ValidatePromoCodeCommand command) {
        // BA-PROMO-VAL-01: Load promo code
        PromoCode promoCode = promoCodeRepository.findByCode(command.getPromoCode())
            .orElse(null);

        if (promoCode == null) {
            return PromoCodeValidationResponse.invalid("ERR-PROMO-NOT-FOUND");
        }

        // BA-PROMO-VAL-02, BA-PROMO-VAL-03: Validate
        Money subtotal = Money.of(command.getSubtotal(), command.getCurrency());
        boolean valid = promoCode.isValid(Instant.now(), subtotal);

        if (!valid) {
            String reason = determineInvalidReason(promoCode, subtotal);
            return PromoCodeValidationResponse.invalid(reason);
        }

        // BA-PROMO-VAL-04: Calculate discount
        Money discount = promoCode.calculateDiscount(subtotal);
        return PromoCodeValidationResponse.valid(discount.toString());
    }

    private String determineInvalidReason(PromoCode promoCode, Money subtotal) {
        Instant now = Instant.now();
        if (now.isBefore(promoCode.getStartDate()) || now.isAfter(promoCode.getEndDate())) {
            return "ERR-PROMO-EXPIRED";
        }
        if (promoCode.getUsageLimit() != null && promoCode.getUsageCount() >= promoCode.getUsageLimit()) {
            return "ERR-PROMO-EXHAUSTED";
        }
        if (promoCode.getMinSubtotal() != null && subtotal.isLessThan(promoCode.getMinSubtotal())) {
            return "ERR-PROMO-MIN-SUBTOTAL";
        }
        return "ERR-PROMO-INVALID";
    }

    private QuoteResponse mapToResponse(Quote quote) {
        QuoteResponse response = new QuoteResponse();
        response.setQuoteId(quote.getQuoteId().toString());
        response.setProductTemplateId(quote.getProductTemplateId());
        response.setBasePrice(quote.getBasePrice().toString());
        response.setOptionPremiums(quote.getOptionPremiums().stream()
            .map(QuoteResponse.PremiumLineDto::from)
            .collect(Collectors.toList()));
        response.setDiscount(quote.getDiscount().toString());
        response.setSubtotal(quote.getSubtotal().toString());
        response.setTax(quote.getTax().toString());
        response.setTotal(quote.getTotal().toString());
        response.setCurrency(quote.getTotal().getCurrency());
        response.setValidUntil(quote.getValidUntil());
        response.setDecisionTrace(quote.getDecisionTrace().stream()
            .map(QuoteResponse.PricingDecisionDto::from)
            .collect(Collectors.toList()));
        return response;
    }
}
