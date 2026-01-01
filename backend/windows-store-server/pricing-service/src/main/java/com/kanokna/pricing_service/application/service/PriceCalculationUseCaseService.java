package com.kanokna.pricing_service.application.service;

import com.kanokna.pricing_service.application.dto.*;
import com.kanokna.pricing_service.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing_service.application.port.in.ValidatePromoCodeUseCase;
import com.kanokna.pricing_service.application.port.out.*;
import com.kanokna.pricing_service.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing_service.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing_service.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing_service.domain.exception.TaxRuleNotFoundException;
import com.kanokna.pricing_service.domain.model.*;
import com.kanokna.pricing_service.domain.service.PriceCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
  </ERROR_HANDLING>

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
@Service
@Transactional
public class PriceCalculationUseCaseService implements CalculateQuoteUseCase, ValidatePromoCodeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PriceCalculationUseCaseService.class);

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
            PriceCalculationService priceCalculationService,
            @Value("${pricing.quote.cache-ttl-minutes:5}") int quoteTtlMinutes) {
        this.priceBookRepository = priceBookRepository;
        this.campaignRepository = campaignRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.taxRuleRepository = taxRuleRepository;
        this.quoteCache = quoteCache;
        this.eventPublisher = eventPublisher;
        this.priceCalculationService = priceCalculationService;
        this.quoteTtlMinutes = quoteTtlMinutes;
    }

    @Override
    public QuoteResponse calculateQuote(CalculateQuoteCommand command) {
        PriceBook priceBook = priceBookRepository
            .findActiveByProductTemplateId(command.getProductTemplateId())
            .orElseThrow(() -> {
                logger.info("[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-01][STATE=LOAD_PRICEBOOK] " +
                        "eventType=PRICING_STEP decision=NOT_FOUND keyValues=productTemplateId={},priceBookId=NONE",
                    command.getProductTemplateId());
                return new PriceBookNotFoundException(command.getProductTemplateId());
            });

        Optional<Quote> cachedQuote = quoteCache.get(priceBook, command);
        if (cachedQuote.isPresent() && !cachedQuote.get().isExpired()) {
            logDecisionTrace(cachedQuote.get());
            return mapToResponse(cachedQuote.get());
        }

        List<Campaign> campaigns = campaignRepository
            .findActiveForProduct(command.getProductTemplateId());

        PromoCode promoCode = null;
        if (command.getPromoCode() != null && !command.getPromoCode().isBlank()) {
            promoCode = promoCodeRepository.findByCode(command.getPromoCode())
                .orElseThrow(() -> {
                    logPromoDecision(command.getPromoCode(), "INVALID");
                    return new InvalidPromoCodeException(command.getPromoCode(), "Promo code not found");
                });
        }

        TaxRule taxRule = taxRuleRepository.findByRegion(command.getRegion())
            .orElseThrow(() -> new TaxRuleNotFoundException(command.getRegion()));

        Quote quote;
        try {
            quote = priceCalculationService.calculateQuote(
                priceBook,
                command.getResolvedBom(),
                command.getWidthCm(),
                command.getHeightCm(),
                campaigns,
                promoCode,
                taxRule,
                quoteTtlMinutes
            );
        } catch (InvalidPromoCodeException ex) {
            logPromoDecision(ex.getPromoCode(), "INVALID");
            throw ex;
        }

        quoteCache.put(priceBook, command, quote, quoteTtlMinutes);

        if (promoCode != null) {
            promoCode.incrementUsage();
            promoCodeRepository.save(promoCode);
        }

        eventPublisher.publishQuoteCalculated(QuoteCalculatedEvent.of(quote, command.getPromoCode()));

        logDecisionTrace(quote);
        return mapToResponse(quote);
    }

    @Override
    public PromoCodeValidationResponse validatePromoCode(ValidatePromoCodeCommand command) {
        PromoCode promoCode = promoCodeRepository.findByCode(command.getPromoCode())
            .orElse(null);

        if (promoCode == null) {
            return PromoCodeValidationResponse.invalid("ERR-PROMO-NOT-FOUND");
        }

        Money subtotal = Money.of(command.getSubtotal(), command.getCurrency());
        boolean valid = promoCode.isValid(Instant.now(), subtotal);

        if (!valid) {
            String reason = determineInvalidReason(promoCode, subtotal);
            return PromoCodeValidationResponse.invalid(reason);
        }

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

    private void logPromoDecision(String promoCode, String decision) {
        logger.info("[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-05][STATE=PROMO] " +
                "eventType=PRICING_STEP decision={} keyValues=promoCode={},discount_rub=0",
            decision, promoCode);
    }

    private void logDecisionTrace(Quote quote) {
        for (PricingDecision decision : quote.getDecisionTrace()) {
            logger.info("[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK={}][STATE={}] {}",
                decision.getStep(),
                decision.getRuleApplied(),
                decision.getResult());
        }
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
