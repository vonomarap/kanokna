package com.kanokna.pricing_service.domain.service;

import com.kanokna.pricing_service.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.pricing_service.domain.model.Campaign;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import com.kanokna.pricing_service.domain.model.Quote;
import com.kanokna.pricing_service.domain.model.QuoteCalculationResult;
import com.kanokna.pricing_service.domain.model.QuoteRequest;
import com.kanokna.pricing_service.domain.model.TaxRule;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;
import com.kanokna.shared.money.MoneyRoundingPolicy;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/* <MODULE_CONTRACT
    id="mod.pricing.domain"
    ROLE="Domain service for pricing calculations"
    SERVICE="pricing-service"
    LAYER="domain.service"
    BOUNDED_CONTEXT="pricing"
    LINKS="backend/windows-store-server/services/pricing-service/docs/RequirementsAnalysis.xml#UC-PRICE-PREVIEW,backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Contracts-calc,backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote">
   <PURPOSE>
     Compute quotes by combining base prices, option premiums, campaigns/discounts, and tax/rounding policies
     while emitting belief-state traces and a QuoteCalculatedEvent for outbox publishing.
   </PURPOSE>
   <RESPONSIBILITIES>
     <Item>Validate price book status and currency alignment with request.</Item>
     <Item>Aggregate base price, options, discounts, and tax with deterministic rounding.</Item>
     <Item>Capture decision traces under anchors PRICE-BASE/OPTIONS/DISCOUNT/TAX.</Item>
   </RESPONSIBILITIES>
   <INVARIANTS>
     <Item>PriceBook must be ACTIVE to price quotes.</Item>
     <Item>Currency for all Money values must match the price book currency.</Item>
   </INVARIANTS>
   <LOGGING>
     <Pattern>[PRICING][calculatePrice][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="PRICE-BASE" purpose="Resolve base price"/>
       <Anchor id="PRICE-OPTIONS" purpose="Sum option premiums"/>
       <Anchor id="PRICE-DISCOUNT" purpose="Apply campaigns/discounts"/>
       <Anchor id="PRICE-TAX" purpose="Apply tax/rounding policy"/>
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-PRICE-001">Valid quote returns totals and QuoteCalculatedEvent.</Case>
     <Case id="TC-PRICE-002">Inactive price book triggers PricingDomainException.</Case>
     <Case id="TC-PRICE-003">Campaign percent applied to subtotal.</Case>
     <Case id="TC-PRICE-004">Tax applied only when includeTax=true and taxRule present.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class PriceCalculationService {

    /* <FUNCTION_CONTRACT
         id="calculatePrice"
         LAYER="domain.service"
         INTENT="Compute quote for a configuration/context"
         INPUT="QuoteRequest request, PriceBook priceBook, Iterable<Campaign> campaigns, TaxRule taxRule, MoneyRoundingPolicy roundingPolicy"
         OUTPUT="QuoteCalculationResult"
         SIDE_EFFECTS="None; returns QuoteCalculatedEvent to be published by application."
         LINKS="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Contracts-calc;backend/windows-store-server/services/pricing-service/docs/RequirementsAnalysis.xml#UC-PRICE-PREVIEW">
       <PRECONDITIONS>
         <Item>priceBook is ACTIVE and matches request currency.</Item>
         <Item>roundingPolicy non-null (default applied if null).</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>QuoteCalculationResult contains Quote + traces; event present when successful.</Item>
       </POSTCONDITIONS>
       <ERROR_HANDLING>
         - PricingDomainException when price book inactive or currency mismatch.
         - PricingDomainException when base price missing for requested item.
       </ERROR_HANDLING>
       <LOGGING>
         - DecisionTrace entries for each block anchor with belief-state detail.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    public QuoteCalculationResult calculatePrice(
        QuoteRequest request,
        PriceBook priceBook,
        Iterable<Campaign> campaigns,
        TaxRule taxRule,
        MoneyRoundingPolicy roundingPolicy
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(priceBook, "priceBook");

        MoneyRoundingPolicy policy = roundingPolicy == null ? MoneyRoundingPolicy.defaultPolicy() : roundingPolicy;

        if (priceBook.status() != PriceBookStatus.ACTIVE) {
            throw new PricingDomainException("Price book must be ACTIVE to calculate quotes");
        }
        ensureCurrencyMatch(request.currency(), priceBook.currency());

        DecisionTrace.TraceCollector traces = new DecisionTrace.TraceCollector();

        /* <BLOCK_ANCHOR id="PRICE-BASE" purpose="Resolve base price"/> */
        Money base = priceBook.basePriceFor(request.itemCode());
        traces.trace("PRICE-BASE", "RESOLVED", "[PRICING][calculatePrice][block=PRICE-BASE][state=RESOLVED] base=" + base.getAmount());

        /* <BLOCK_ANCHOR id="PRICE-OPTIONS" purpose="Sum option premiums"/> */
        Money optionsTotal = sumOptionPremiums(request, priceBook, policy, traces);

        Money subtotal = base.add(optionsTotal, policy);

        /* <BLOCK_ANCHOR id="PRICE-DISCOUNT" purpose="Apply campaigns/discounts"/> */
        DiscountResult discountResult = applyCampaigns(subtotal, campaigns, policy, traces);

        Money taxableBase = subtotal.subtract(discountResult.discount(), policy);

        /* <BLOCK_ANCHOR id="PRICE-TAX" purpose="Apply tax/rounding policy"/> */
        Money tax = Money.zero(priceBook.currency());
        if (request.includeTax() && taxRule != null) {
            tax = taxRule.taxFor(taxableBase, policy);
            traces.trace("PRICE-TAX", "APPLIED", "[PRICING][calculatePrice][block=PRICE-TAX][state=APPLIED] tax=" + tax.getAmount());
        } else {
            traces.trace("PRICE-TAX", "SKIPPED", "[PRICING][calculatePrice][block=PRICE-TAX][state=SKIPPED]");
        }

        Money total = taxableBase.add(tax, policy);

        Quote quote = new Quote(
            Id.random(),
            priceBook.id(),
            priceBook.version(),
            request.catalogVersion(),
            priceBook.region(),
            priceBook.currency(),
            base,
            optionsTotal,
            discountResult.discount(),
            tax,
            total,
            discountResult.appliedCampaignIds()
        );

        QuoteCalculatedEvent event = QuoteCalculatedEvent.of(priceBook.id(), priceBook.version(), quote.id(), total);

        return new QuoteCalculationResult(quote, traces.asImmutable(), event);
    }

    private Money sumOptionPremiums(QuoteRequest request, PriceBook priceBook, MoneyRoundingPolicy policy, DecisionTrace.TraceCollector traces) {
        Money total = Money.zero(priceBook.currency());
        for (var entry : request.optionSelections().entrySet()) {
            Money premium = priceBook.optionPremiumFor(entry.getKey(), entry.getValue());
            total = total.add(premium, policy);
        }
        traces.trace("PRICE-OPTIONS", "SUMMED", "[PRICING][calculatePrice][block=PRICE-OPTIONS][state=SUMMED] total=" + total.getAmount());
        return total;
    }

    private DiscountResult applyCampaigns(Money subtotal, Iterable<Campaign> campaigns, MoneyRoundingPolicy policy, DecisionTrace.TraceCollector traces) {
        Money totalDiscount = Money.zero(subtotal.getCurrency());
        List<Id> applied = new ArrayList<>();

        for (Campaign campaign : campaigns) {
            Money discount = campaign.discountFor(subtotal, policy, java.time.Instant.now(), traces);
            totalDiscount = totalDiscount.add(discount, policy);
            if (!discount.isZero()) {
                applied.add(campaign.id());
            }
        }
        traces.trace("PRICE-DISCOUNT", "TOTAL", "[PRICING][calculatePrice][block=PRICE-DISCOUNT][state=TOTAL] discount=" + totalDiscount.getAmount());
        return new DiscountResult(totalDiscount, applied);
    }

    private void ensureCurrencyMatch(Currency requestCurrency, Currency priceBookCurrency) {
        if (!requestCurrency.equals(priceBookCurrency)) {
            throw new PricingDomainException("Currency mismatch: request=%s priceBook=%s"
                .formatted(requestCurrency.getCurrencyCode(), priceBookCurrency.getCurrencyCode()));
        }
    }

    private record DiscountResult(Money discount, List<Id> appliedCampaignIds) { }
}
