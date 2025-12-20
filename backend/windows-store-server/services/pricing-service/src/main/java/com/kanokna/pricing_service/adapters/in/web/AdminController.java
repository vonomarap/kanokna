package com.kanokna.pricing_service.adapters.in.web;

import com.kanokna.pricing_service.application.dto.CampaignCommand;
import com.kanokna.pricing_service.application.dto.PriceBookCommand;
import com.kanokna.pricing_service.application.dto.TaxRuleCommand;
import com.kanokna.pricing_service.application.port.in.PriceAdminPort;
import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pricing/admin")
public class AdminController {

    private final PriceAdminPort adminPort;

    public AdminController(PriceAdminPort adminPort) {
        this.adminPort = adminPort;
    }

    @PostMapping("/pricebooks")
    public ResponseEntity<Void> upsertPriceBook(@Valid @RequestBody PriceBookRequest request) {
        PriceBookCommand command = new PriceBookCommand(
            request.id() == null ? null : Id.of(request.id()),
            request.region(),
            Currency.getInstance(request.currency()),
            PriceBookStatus.valueOf(request.status()),
            request.basePrices().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Money.of(e.getValue(), Currency.getInstance(request.currency())))),
            request.optionPremiums().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Money.of(e.getValue(), Currency.getInstance(request.currency()))))
        );
        adminPort.createOrUpdatePriceBook(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pricebooks/publish")
    public ResponseEntity<Void> publish(@RequestBody PublishPriceBookRequest request) {
        adminPort.publishPriceBook(request.priceBookId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/campaigns")
    public ResponseEntity<Void> upsertCampaign(@Valid @RequestBody CampaignRequest request) {
        CampaignCommand command = new CampaignCommand(
            request.id() == null ? null : Id.of(request.id()),
            request.name(),
            request.status(),
            request.percentOff(),
            request.startsAt(),
            request.endsAt()
        );
        adminPort.defineCampaign(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tax-rules")
    public ResponseEntity<Void> upsertTaxRule(@Valid @RequestBody TaxRuleRequest request) {
        adminPort.updateTaxRules(new TaxRuleCommand(
            request.region(),
            request.productType(),
            request.rate(),
            request.roundingPolicyCode()
        ));
        return ResponseEntity.ok().build();
    }

    public record PriceBookRequest(
        String id,
        @NotBlank String region,
        @NotBlank String currency,
        @NotBlank String status,
        Map<String, BigDecimal> basePrices,
        Map<String, BigDecimal> optionPremiums
    ) {}

    public record PublishPriceBookRequest(@NotBlank String priceBookId) {}

    public record CampaignRequest(
        String id,
        @NotBlank String name,
        com.kanokna.pricing_service.domain.model.CampaignStatus status,
        @NotBlank BigDecimal percentOff,
        java.time.Instant startsAt,
        java.time.Instant endsAt
    ) {}

    public record TaxRuleRequest(
        @NotBlank String region,
        String productType,
        @NotBlank BigDecimal rate,
        String roundingPolicyCode
    ) {}
}
