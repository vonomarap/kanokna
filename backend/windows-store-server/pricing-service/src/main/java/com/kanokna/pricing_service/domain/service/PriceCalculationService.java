package com.kanokna.pricing_service.domain.service;

import com.kanokna.pricing_service.domain.model.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* <MODULE_CONTRACT id="MC-pricing-service-domain-PriceCalculation"
     ROLE="DomainService"
     SERVICE="pricing-service"
     LAYER="domain"
     BOUNDED_CONTEXT="pricing"
     SPECIFICATION="UC-PRICING-QUOTE">
  <PURPOSE>
    Domain service that calculates price quotes for validated product configurations.
    Orchestrates base price calculation, option premiums, discounts, and taxes to
    produce itemized quotes with full decision trace for auditability.
  </PURPOSE>

  <RESPONSIBILITIES>
    <Item>Load active price book for product template</Item>
    <Item>Calculate base price from area (m²) and price_per_m2</Item>
    <Item>Apply option premiums (ABSOLUTE or PERCENTAGE)</Item>
    <Item>Apply campaign discounts (if active campaigns exist)</Item>
    <Item>Apply promo code discounts (if provided and valid)</Item>
    <Item>Enforce discount cap (30% max combined)</Item>
    <Item>Calculate regional tax (VAT)</Item>
    <Item>Apply currency-specific rounding</Item>
    <Item>Build decision trace for audit</Item>
  </RESPONSIBILITIES>

  <INVARIANTS>
    <Item>Quote calculation is deterministic for same inputs</Item>
    <Item>Base price is always positive</Item>
    <Item>Combined discounts never exceed 30% of subtotal</Item>
    <Item>Tax is calculated on discounted subtotal</Item>
    <Item>All amounts use correct currency precision</Item>
  </INVARIANTS>

  <CONTEXT>
    <UPSTREAM>
      <Item>PricingGrpcService: receives requests from catalog-configuration-service</Item>
      <Item>PricingController: REST API for direct quote requests</Item>
    </UPSTREAM>
    <DOWNSTREAM>
      <Item>PriceBookRepository: loads price books</Item>
      <Item>CampaignRepository: loads active campaigns</Item>
      <Item>PromoCodeRepository: validates promo codes</Item>
      <Item>TaxRuleRepository: loads tax rules</Item>
      <Item>QuoteCache: caches calculated quotes</Item>
    </DOWNSTREAM>
  </CONTEXT>

  <PUBLIC_API>
    <Method name="calculateQuote" input="CalculateQuoteCommand" output="Quote"/>
  </PUBLIC_API>

  <BUSINESS_RULES>
    <Rule id="BR-PRC-001">Base price = max(area_m2, minimum_area_m2) * price_per_m2 (minimum_area_m2 default: 0.25m²)</Rule>
    <Rule id="BR-PRC-002">ABSOLUTE premiums add fixed amount</Rule>
    <Rule id="BR-PRC-003">PERCENTAGE premiums add (base_price * percentage / 100)</Rule>
    <Rule id="BR-PRC-004">Campaign discounts apply to subtotal before promo codes</Rule>
    <Rule id="BR-PRC-005">Combined discounts capped at 30%</Rule>
    <Rule id="BR-PRC-006">Russia VAT is 20% on discounted subtotal</Rule>
    <Rule id="BR-PRC-007">Final price rounded HALF_UP to 2 decimals for RUB</Rule>
  </BUSINESS_RULES>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="ERR-PRC-NO-PRICEBOOK">No active price book for product</Item>
    <Item type="BUSINESS" code="ERR-PRC-INVALID-PROMO">Promo code invalid, expired, or exhausted</Item>
    <Item type="BUSINESS" code="ERR-PRC-NO-TAXRULE">No tax rule for region</Item>
    <Item type="BUSINESS" code="ERR-PRC-DISCOUNT-EXCEEDED">Combined discounts exceed 30% cap</Item>
    <Item type="TECHNICAL" code="ERR-PRC-CALCULATION-FAILED">Unexpected calculation error</Item>
  </ERROR_HANDLING>

  <LOGGING>
    <FORMAT>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=...][STATE=...] eventType=... decision=... keyValues=...</FORMAT>
    <EXAMPLES>
      <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-01][STATE=LOAD_PRICEBOOK] eventType=PRICING_STEP decision=FOUND keyValues=priceBookId,productTemplateId</Item>
      <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-99][STATE=FINAL] eventType=QUOTE_CALCULATED decision=SUCCESS keyValues=quoteId,total_rub</Item>
    </EXAMPLES>
  </LOGGING>

  <TESTS>
    <Case id="TC-PRC-001">Standard configuration returns correct base price</Case>
    <Case id="TC-PRC-002">Option premiums (ABSOLUTE) added correctly</Case>
    <Case id="TC-PRC-003">Option premiums (PERCENTAGE) calculated on base price</Case>
    <Case id="TC-PRC-004">Active campaign discount applied</Case>
    <Case id="TC-PRC-005">Valid promo code discount applied</Case>
    <Case id="TC-PRC-006">Campaign + promo stacking respects 30% cap</Case>
    <Case id="TC-PRC-007">Russia VAT (20%) calculated correctly</Case>
    <Case id="TC-PRC-008">Missing price book returns ERR-PRC-NO-PRICEBOOK</Case>
    <Case id="TC-PRC-009">Invalid promo code returns ERR-PRC-INVALID-PROMO</Case>
    <Case id="TC-PRC-010">Expired campaign not applied</Case>
    <Case id="TC-PRC-011">Quote cached and retrieved on repeat request</Case>
    <Case id="TC-PRC-012">Decision trace contains all calculation steps</Case>
  </TESTS>

  <LINKS>
    <Link ref="RequirementsAnalysis.xml#UC-PRICING-QUOTE"/>
    <Link ref="DevelopmentPlan.xml#DP-SVC-pricing-service"/>
    <Link ref="DevelopmentPlan.xml#Flow-Config-Pricing"/>
    <Link ref="Technology.xml#DEC-ARCH-RULE-ENGINE-STRATEGY"/>
  </LINKS>
</MODULE_CONTRACT> */

/* <FUNCTION_CONTRACT id="FC-pricing-service-UC-PRICING-QUOTE-calculateQuote"
     LAYER="domain.service"
     INTENT="Calculate a complete price quote for a validated product configuration"
     INPUT="CalculateQuoteCommand (productTemplateId, dimensions, resolvedBom, currency, promoCode?, region)"
     OUTPUT="Quote"
     SIDE_EFFECTS="Cache quote in Redis, publish QuoteCalculatedEvent"
     LINKS="RequirementsAnalysis.xml#UC-PRICING-QUOTE;DevelopmentPlan.xml#Flow-Config-Pricing">
  <PRECONDITIONS>
    <Item>command.productTemplateId is not null</Item>
    <Item>command.dimensions is valid (50-400cm range)</Item>
    <Item>command.resolvedBom is not empty</Item>
    <Item>command.currency is supported (RUB initially)</Item>
    <Item>command.region is provided (default: "RU")</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>Quote is never null</Item>
    <Item>Quote.total equals base_price + premiums - discounts + tax</Item>
    <Item>Quote.decisionTrace contains all calculation steps</Item>
    <Item>Quote.validUntil is 5 minutes from calculation</Item>
    <Item>Quote is cached in Redis with quoteId as key</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Calculation is deterministic for same inputs and price book version</Item>
    <Item>All Money values use same currency</Item>
    <Item>Rounding applied only at final step</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="ERR-PRC-NO-PRICEBOOK">No active price book found</Item>
    <Item type="BUSINESS" code="ERR-PRC-INVALID-PROMO">Promo code invalid or expired</Item>
    <Item type="BUSINESS" code="ERR-PRC-NO-TAXRULE">No tax rule for region</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-PRC-CALC-01">Load price book for product</Item>
    <Item id="BA-PRC-CALC-02">Calculate base price from dimensions</Item>
    <Item id="BA-PRC-CALC-03">Apply option premiums</Item>
    <Item id="BA-PRC-CALC-04">Apply campaign discounts</Item>
    <Item id="BA-PRC-CALC-05">Apply promo code discount</Item>
    <Item id="BA-PRC-CALC-06">Calculate subtotal</Item>
    <Item id="BA-PRC-CALC-07">Calculate tax</Item>
    <Item id="BA-PRC-CALC-08">Apply rounding</Item>
    <Item id="BA-PRC-CALC-99">Final quote result</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-01][STATE=LOAD_PRICEBOOK] eventType=PRICING_STEP decision=FOUND|NOT_FOUND keyValues=productTemplateId,priceBookId</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-02][STATE=BASE_PRICE] eventType=PRICING_STEP decision=CALCULATED keyValues=area_m2,price_per_m2,base_price_rub</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-03][STATE=PREMIUMS] eventType=PRICING_STEP decision=APPLIED keyValues=premium_count,total_premium_rub</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-04][STATE=CAMPAIGN] eventType=PRICING_STEP decision=APPLIED|NONE keyValues=campaignId,discount_rub</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-05][STATE=PROMO] eventType=PRICING_STEP decision=APPLIED|INVALID|NONE keyValues=promoCode,discount_rub</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-07][STATE=TAX] eventType=PRICING_STEP decision=CALCULATED keyValues=region,tax_rate_pct,tax_rub</Item>
    <Item>[SVC=pricing-service][UC=UC-PRICING-QUOTE][BLOCK=BA-PRC-CALC-99][STATE=FINAL] eventType=QUOTE_CALCULATED decision=SUCCESS keyValues=quoteId,total_rub,valid_until</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-FUNC-CALC-001">Valid configuration returns quote with all fields populated</Case>
    <Case id="TC-FUNC-CALC-002">Area calculated correctly from dimensions (width_cm * height_cm / 10000)</Case>
    <Case id="TC-FUNC-CALC-003">Minimum area charge applied for small configurations</Case>
    <Case id="TC-FUNC-CALC-004">Multiple ABSOLUTE premiums summed correctly</Case>
    <Case id="TC-FUNC-CALC-005">PERCENTAGE premium calculated on base price only</Case>
    <Case id="TC-FUNC-CALC-006">Best campaign discount selected when multiple active</Case>
    <Case id="TC-FUNC-CALC-007">Promo code validates usage limits</Case>
    <Case id="TC-FUNC-CALC-008">30% discount cap enforced</Case>
    <Case id="TC-FUNC-CALC-009">Tax calculated on discounted subtotal</Case>
    <Case id="TC-FUNC-CALC-010">Rounding applied consistently</Case>
    <Case id="TC-FUNC-CALC-011">Quote cached with correct TTL</Case>
    <Case id="TC-FUNC-CALC-012">Cached quote returned for identical inputs</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

/**
 * Domain service for calculating price quotes.
 * Pure business logic, framework-independent.
 */
public class PriceCalculationService {

    private final DiscountService discountService;
    private final TaxCalculationService taxCalculationService;
    private final RoundingService roundingService;

    public PriceCalculationService(DiscountService discountService,
                                  TaxCalculationService taxCalculationService,
                                  RoundingService roundingService) {
        this.discountService = java.util.Objects.requireNonNull(discountService);
        this.taxCalculationService = java.util.Objects.requireNonNull(taxCalculationService);
        this.roundingService = java.util.Objects.requireNonNull(roundingService);
    }

        public Quote calculateQuote(PriceBook priceBook, java.util.List<String> selectedOptionIds,
                               java.math.BigDecimal widthCm, java.math.BigDecimal heightCm,
                               java.util.List<Campaign> activeCampaigns, PromoCode promoCode,
                               TaxRule taxRule, int quoteTtlMinutes) {

        java.util.List<PricingDecision> decisionTrace = new java.util.ArrayList<>();
        String currency = priceBook.getCurrency();

        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-01",
            "LOAD_PRICEBOOK",
            String.format("eventType=PRICING_STEP decision=FOUND keyValues=productTemplateId=%s,priceBookId=%s",
                priceBook.getProductTemplateId(), priceBook.getId())
        ));

        java.util.List<String> optionIds = selectedOptionIds != null ? selectedOptionIds : java.util.List.of();

        // BA-PRC-CALC-02: Calculate base price from dimensions
        java.math.BigDecimal areaM2 = widthCm.multiply(heightCm)
            .divide(new java.math.BigDecimal("10000"), 4, java.math.RoundingMode.HALF_UP);
        Money basePrice = priceBook.getBasePriceEntry().calculateBasePrice(areaM2, currency);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-02",
            "BASE_PRICE",
            String.format("eventType=PRICING_STEP decision=CALCULATED keyValues=area_m2=%s,price_per_m2=%s,base_price_rub=%s",
                areaM2.toPlainString(),
                priceBook.getBasePriceEntry().getPricePerM2().toPlainString(),
                basePrice.getAmount().toPlainString())
        ));

        // BA-PRC-CALC-03: Apply option premiums
        java.util.List<PremiumLine> premiumLines = new java.util.ArrayList<>();
        Money totalPremiums = Money.zero(currency);
        for (String optionId : optionIds) {
            java.util.Optional<OptionPremium> premium = priceBook.findPremiumForOption(optionId);
            if (premium.isPresent()) {
                Money premiumAmount = premium.get().calculatePremium(basePrice);
                premiumLines.add(PremiumLine.of(optionId, premium.get().getOptionName(), premiumAmount));
                totalPremiums = totalPremiums.add(premiumAmount);
            }
        }
        Money priceWithPremiums = basePrice.add(totalPremiums);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-03",
            "PREMIUMS",
            String.format("eventType=PRICING_STEP decision=APPLIED keyValues=premium_count=%d,total_premium_rub=%s",
                premiumLines.size(), totalPremiums.getAmount().toPlainString())
        ));

        // BA-PRC-CALC-04 & BA-PRC-CALC-05: Apply discounts
        Money totalDiscount = discountService.calculateTotalDiscount(
            priceWithPremiums, activeCampaigns, promoCode, decisionTrace
        );

        // BA-PRC-CALC-06: Calculate subtotal
        Money discountedSubtotal = priceWithPremiums.subtract(totalDiscount);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-06",
            "SUBTOTAL",
            String.format("eventType=PRICING_STEP decision=CALCULATED keyValues=subtotal_rub=%s,discount_rub=%s",
                discountedSubtotal.getAmount().toPlainString(), totalDiscount.getAmount().toPlainString())
        ));

        // BA-PRC-CALC-07: Calculate tax
        Money tax = taxCalculationService.calculateTax(discountedSubtotal, taxRule, decisionTrace);

        // BA-PRC-CALC-08: Apply rounding
        Money total = roundingService.round(discountedSubtotal.add(tax), currency, decisionTrace);

        QuoteId quoteId = QuoteId.generate();
        java.time.Instant validUntil = java.time.Instant.now().plus(java.time.Duration.ofMinutes(quoteTtlMinutes));
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-99",
            "FINAL",
            String.format("eventType=QUOTE_CALCULATED decision=SUCCESS keyValues=quoteId=%s,total_rub=%s,valid_until=%s",
                quoteId, total.getAmount().toPlainString(), validUntil)
        ));

        return Quote.builder()
            .quoteId(quoteId)
            .productTemplateId(priceBook.getProductTemplateId())
            .basePrice(basePrice)
            .optionPremiums(premiumLines)
            .discount(totalDiscount)
            .subtotal(discountedSubtotal)
            .tax(tax)
            .total(total)
            .validUntil(validUntil)
            .decisionTrace(decisionTrace)
            .build();
    }
}
