package com.kanokna.pricing_service.adapters.in.web;

import com.kanokna.pricing_service.adapters.in.web.dto.DecisionTraceDto;
import com.kanokna.pricing_service.adapters.in.web.dto.MoneyDto;
import com.kanokna.pricing_service.adapters.in.web.dto.QuoteRequestDto;
import com.kanokna.pricing_service.adapters.in.web.dto.QuoteResponseDto;
import com.kanokna.pricing_service.application.dto.QuoteConfigurationCommand;
import com.kanokna.pricing_service.application.dto.QuoteResponse;
import com.kanokna.pricing_service.application.port.in.QuotePort;
import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.Map;
import java.util.stream.Collectors;

/* <MODULE_CONTRACT
    id="mod.pricing.adapters.in.web"
    ROLE="REST adapter for pricing quote endpoints"
    SERVICE="pricing-service"
    LAYER="adapters.in.web"
    LINKS="backend/windows-store-server/services/pricing-service/docs/pricing-openapi.yaml,backend/windows-store-server/services/pricing-service/docs/DevelopmentPlan.xml#Flow-Quote">
   <PURPOSE>
     Expose HTTP endpoints for quote calculation, mapping DTOs to QuotePort commands and returning structured responses with belief-state traces.
   </PURPOSE>
   <PUBLIC_API>
     - POST /api/v1/pricing/quote
     - POST /api/v1/pricing/quote/cart
   </PUBLIC_API>
   <LOGGING>
     - INFO [HTTP][pricing][quote][state=START|DONE|ERROR]
   </LOGGING>
 </MODULE_CONTRACT> */
@Validated
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);
    private final QuotePort quotePort;

    public PricingController(QuotePort quotePort) {
        this.quotePort = quotePort;
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponseDto> quote(@Valid @RequestBody QuoteRequestDto request) {
        logger.info("[HTTP][pricing][quote][state=START] item={} region={}", request.itemCode(), request.region());
        QuoteResponse response = quotePort.quoteConfiguration(toCommand(request));
        QuoteResponseDto dto = toDto(response);
        logger.info("[HTTP][pricing][quote][state=DONE] quoteId={} total={}", dto.quoteId(), dto.total().amount());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/quote/cart")
    public ResponseEntity<QuoteResponseDto> quoteCart(@Valid @RequestBody QuoteRequestDto request) {
        return quote(request);
    }

    @ExceptionHandler(PricingDomainException.class)
    public ResponseEntity<Map<String, String>> handleDomain(PricingDomainException ex) {
        logger.warn("[HTTP][pricing][quote][state=ERROR] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    private QuoteConfigurationCommand toCommand(QuoteRequestDto request) {
        return new QuoteConfigurationCommand(
            request.priceBookId() == null || request.priceBookId().isBlank() ? null : Id.of(request.priceBookId()),
            request.region(),
            Currency.getInstance(request.currency()),
            request.itemCode(),
            normalizeOptions(request.optionSelections()),
            request.customerSegment(),
            request.catalogVersion(),
            request.includeTax(),
            request.idempotencyKey()
        );
    }

    private Map<String, String> normalizeOptions(Map<String, String> options) {
        if (options == null) {
            return Map.of();
        }
        return options.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
            .collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().trim(),
                entry -> entry.getValue().trim()
            ));
    }

    private QuoteResponseDto toDto(QuoteResponse response) {
        return new QuoteResponseDto(
            response.quoteId().value(),
            response.priceBookId().value(),
            response.priceBookVersion(),
            response.catalogVersion(),
            response.region(),
            money(response.base()),
            money(response.optionsTotal()),
            money(response.discountTotal()),
            money(response.taxTotal()),
            money(response.total()),
            response.appliedCampaigns().stream().map(Id::value).toList(),
            response.traces().stream()
                .map(trace -> new DecisionTraceDto(trace.blockId(), trace.state(), trace.detail()))
                .toList()
        );
    }

    private MoneyDto money(Money money) {
        return new MoneyDto(money.getAmount(), money.getCurrency().getCurrencyCode());
    }
}
