package com.kanokna.pricing_service.application.service;

import com.kanokna.pricing_service.application.dto.QuoteConfigurationCommand;
import com.kanokna.pricing_service.application.dto.QuoteResponse;
import com.kanokna.pricing_service.application.port.in.QuotePort;
import com.kanokna.pricing_service.application.port.out.CampaignRepository;
import com.kanokna.pricing_service.application.port.out.OutboxPublisher;
import com.kanokna.pricing_service.application.port.out.PriceBookRepository;
import com.kanokna.pricing_service.application.port.out.QuoteCache;
import com.kanokna.pricing_service.application.port.out.TaxRuleRepository;
import com.kanokna.pricing_service.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.pricing_service.domain.model.Campaign;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.Quote;
import com.kanokna.pricing_service.domain.model.QuoteCalculationResult;
import com.kanokna.pricing_service.domain.model.QuoteRequest;
import com.kanokna.pricing_service.domain.model.TaxRule;
import com.kanokna.pricing_service.domain.service.DecisionTrace;
import com.kanokna.pricing_service.domain.service.PriceCalculationService;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/* <MODULE_CONTRACT
    id="mod.pricing.application"
    ROLE="Application service orchestrating quote use cases"
    SERVICE="pricing-service"
    LAYER="application"
    BOUNDED_CONTEXT="pricing"
    LINKS="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote,backend/windows-store-server/services/pricing-service/docs/RequirementsAnalysis.xml#UC-PRICE-PREVIEW">
   <PURPOSE>
     Handle quote requests by loading required aggregates, delegating to domain calculation, caching results, and publishing events.
   </PURPOSE>
   <LOGGING>
     <Pattern>[APP][pricing][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="APP-QUOTE-LOAD" purpose="Load price book and request data" />
       <Anchor id="APP-QUOTE-CACHE" purpose="Check/put cache" />
       <Anchor id="APP-QUOTE-DOMAIN" purpose="Invoke domain calculation" />
       <Anchor id="APP-QUOTE-OUTBOX" purpose="Publish QuoteCalculatedEvent" />
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-APP-QUOTE-001">Uses cache when idempotency key hit.</Case>
     <Case id="TC-APP-QUOTE-002">Throws when price book not found.</Case>
     <Case id="TC-APP-QUOTE-003">Publishes event on successful calculation.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class PriceApplicationService implements QuotePort {

    private final PriceBookRepository priceBookRepository;
    private final CampaignRepository campaignRepository;
    private final TaxRuleRepository taxRuleRepository;
    private final QuoteCache quoteCache;
    private final OutboxPublisher outboxPublisher;
    private final PriceCalculationService calculationService;

    public PriceApplicationService(
        PriceBookRepository priceBookRepository,
        CampaignRepository campaignRepository,
        TaxRuleRepository taxRuleRepository,
        QuoteCache quoteCache,
        OutboxPublisher outboxPublisher,
        PriceCalculationService calculationService
    ) {
        this.priceBookRepository = Objects.requireNonNull(priceBookRepository, "priceBookRepository");
        this.campaignRepository = Objects.requireNonNull(campaignRepository, "campaignRepository");
        this.taxRuleRepository = Objects.requireNonNull(taxRuleRepository, "taxRuleRepository");
        this.quoteCache = Objects.requireNonNull(quoteCache, "quoteCache");
        this.outboxPublisher = Objects.requireNonNull(outboxPublisher, "outboxPublisher");
        this.calculationService = Objects.requireNonNull(calculationService, "calculationService");
    }

    /* <FUNCTION_CONTRACT
         id="quoteConfiguration.app"
         LAYER="application.service"
         INTENT="Provide quote for configuration using price book, campaigns, and tax rules"
         INPUT="QuoteConfigurationCommand"
         OUTPUT="QuoteResponse"
         SIDE_EFFECTS="Cache lookup/write; publish QuoteCalculatedEvent"
         LINKS="backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote;backend/windows-store-server/services/pricing-service/docs/RequirementsAnalysis.xml#UC-PRICE-PREVIEW">
       <PRECONDITIONS>
         <Item>Command contains region, currency, itemCode, optionSelections (non-null).</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>OutboxPublisher invoked when domain returns event.</Item>
         <Item>Quote cached by idempotency key if provided.</Item>
       </POSTCONDITIONS>
       <LOGGING>
         - Use APP-QUOTE-LOAD/APP-QUOTE-CACHE/APP-QUOTE-DOMAIN/APP-QUOTE-OUTBOX anchors for adapter logs.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    @Override
    public QuoteResponse quoteConfiguration(QuoteConfigurationCommand command) {
        DecisionTrace.TraceCollector traces = new DecisionTrace.TraceCollector();

        /* <BLOCK_ANCHOR id="APP-QUOTE-LOAD" purpose="Resolve price book and request" /> */
        PriceBook priceBook = resolvePriceBook(command);
        QuoteRequest request = toQuoteRequest(command, priceBook);

        /* <BLOCK_ANCHOR id="APP-QUOTE-CACHE" purpose="Check cached quote" /> */
        String cacheKey = cacheKey(command);
        if (!cacheKey.isBlank()) {
            Optional<Quote> cached = quoteCache.get(cacheKey);
            if (cached.isPresent()) {
                traces.trace("APP-QUOTE-CACHE", "HIT", "[APP][pricing][block=APP-QUOTE-CACHE][state=HIT]");
                return QuoteResponse.from(cached.get(), traces.asImmutable());
            }
            traces.trace("APP-QUOTE-CACHE", "MISS", "[APP][pricing][block=APP-QUOTE-CACHE][state=MISS]");
        }

        /* <BLOCK_ANCHOR id="APP-QUOTE-DOMAIN" purpose="Invoke domain calculation" /> */
        List<Campaign> campaigns = campaignRepository.findActiveForRegionAndSegment(priceBook.region(), command.customerSegment());
        TaxRule taxRule = taxRuleRepository.findForRegion(priceBook.region()).orElse(null);
        QuoteCalculationResult result = calculationService.calculatePrice(request, priceBook, campaigns, taxRule, null);

        /* <BLOCK_ANCHOR id="APP-QUOTE-OUTBOX" purpose="Publish QuoteCalculatedEvent" /> */
        result.event().ifPresent(outboxPublisher::publish);

        if (!cacheKey.isBlank()) {
            quoteCache.put(cacheKey, result.quote());
        }

        return QuoteResponse.from(result.quote(), mergeTraces(traces.asImmutable(), result.traces()));
    }

    private PriceBook resolvePriceBook(QuoteConfigurationCommand command) {
        return command.priceBookIdOptional()
            .flatMap(priceBookRepository::findActiveById)
            .or(() -> priceBookRepository.findActiveByRegionAndCurrency(command.region(), command.currency()))
            .orElseThrow(() -> new PricingDomainException("Active price book not found for region/currency"));
    }

    private QuoteRequest toQuoteRequest(QuoteConfigurationCommand command, PriceBook priceBook) {
        Map<String, String> normalized = command.optionSelections() == null ? Map.of() : Map.copyOf(command.optionSelections());
        return new QuoteRequest(
            priceBook.id(),
            priceBook.region(),
            priceBook.currency(),
            command.itemCode(),
            normalized,
            command.customerSegment(),
            command.catalogVersion(),
            command.includeTax()
        );
    }

    private String cacheKey(QuoteConfigurationCommand command) {
        return command.idempotencyKey() == null ? "" : command.idempotencyKey().trim();
    }

    private List<DecisionTrace> mergeTraces(List<DecisionTrace> applicationTraces, List<DecisionTrace> domainTraces) {
        List<DecisionTrace> merged = new java.util.ArrayList<>(applicationTraces);
        merged.addAll(domainTraces);
        return List.copyOf(merged);
    }
}
