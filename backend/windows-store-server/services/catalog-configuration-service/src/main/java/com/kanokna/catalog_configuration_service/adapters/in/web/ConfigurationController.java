package com.kanokna.catalog_configuration_service.adapters.in.web;

import com.kanokna.catalog_configuration_service.adapters.in.web.dto.*;
import com.kanokna.catalog_configuration_service.application.dto.ResolveBomCommand;
import com.kanokna.catalog_configuration_service.application.dto.ResolveBomResponse;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationResponse;
import com.kanokna.catalog_configuration_service.application.port.in.QueryPort;
import com.kanokna.catalog_configuration_service.domain.exception.CatalogDomainException;
import com.kanokna.catalog_configuration_service.domain.model.ValidationMessage;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.measure.DimensionsCm;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* <MODULE_CONTRACT
    id="mod.catalog.adapters.in.web"
    ROLE="REST adapter for configuration validation and BOM resolution"
    SERVICE="catalog-configuration-service"
    LAYER="adapters.in.web"
    LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/Technology.xml#Interfaces,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Validation,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Bom">
   <PURPOSE>
     Expose HTTP endpoints for validation and BOM resolution, translating DTOs to application commands and returning DTO responses with belief-state traces.
   </PURPOSE>
   <PUBLIC_API>
     - POST /api/v1/catalog/configurations/validate : ValidateConfigurationRequest -> ValidateConfigurationResponseDto
     - POST /api/v1/catalog/configurations/resolve-bom : ResolveBomRequest -> ResolveBomResponseDto
   </PUBLIC_API>
   <CROSS_CUTTING>
     <SECURITY>Relies on upstream resource server; assume JWT already validated by Spring Security configuration.</SECURITY>
     <OBSERVABILITY>Logs structured markers [HTTP][CFG][useCase][state=*]; adapters propagate DecisionTrace from domain.</OBSERVABILITY>
   </CROSS_CUTTING>
   <LOGGING>
     - INFO [HTTP][CFG][validate][state=START|DONE|ERROR]
     - INFO [HTTP][CFG][resolve-bom][state=START|DONE|ERROR]
   </LOGGING>
 </MODULE_CONTRACT> */
@Validated
@RestController
@RequestMapping("/api/v1/catalog")
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    private final QueryPort queryPort;

    public ConfigurationController(QueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @PostMapping("/configurations/validate")
    public ResponseEntity<ValidateConfigurationResponseDto> validateConfiguration(
        @Valid @RequestBody ValidateConfigurationRequest request
    ) {
        logger.info("[HTTP][CFG][validate][state=START] templateId={} tenantId={}", request.templateId(), request.tenantId());
        ValidateConfigurationResponse appResponse = queryPort.validateConfiguration(toValidateCommand(request));
        ValidateConfigurationResponseDto response = new ValidateConfigurationResponseDto(
            appResponse.valid(),
            mapMessages(appResponse.errors()),
            mapMessages(appResponse.warnings()),
            appResponse.traces().stream()
                .map(trace -> new DecisionTraceDto(trace.blockId(), trace.state(), trace.detail()))
                .toList()
        );
        logger.info("[HTTP][CFG][validate][state=DONE] templateId={} valid={} errors={}", request.templateId(), response.valid(), response.errors().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/configurations/resolve-bom")
    public ResponseEntity<ResolveBomResponseDto> resolveBom(
        @Valid @RequestBody ResolveBomRequest request
    ) {
        logger.info("[HTTP][CFG][resolve-bom][state=START] templateId={} tenantId={}", request.templateId(), request.tenantId());
        ResolveBomResponse appResponse = queryPort.resolveBom(toResolveBomCommand(request));
        ResolveBomResponseDto response = new ResolveBomResponseDto(
            appResponse.templateId().value(),
            appResponse.bomTemplateCode(),
            appResponse.items().stream()
                .map(item -> new BomItemDto(item.sku(), item.quantity(), item.unit()))
                .toList()
        );
        logger.info("[HTTP][CFG][resolve-bom][state=DONE] templateId={} items={}", request.templateId(), response.items().size());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(CatalogDomainException.class)
    public ResponseEntity<Map<String, String>> handleDomain(CatalogDomainException ex) {
        logger.warn("[HTTP][CFG][state=ERROR] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    private ValidateConfigurationCommand toValidateCommand(ValidateConfigurationRequest request) {
        return new ValidateConfigurationCommand(
            Id.of(request.templateId()),
            Id.of(request.tenantId()),
            new DimensionsCm(request.widthCm(), request.heightCm()),
            safeOptions(request.optionSelections()),
            request.locale(),
            request.catalogVersion()
        );
    }

    private ResolveBomCommand toResolveBomCommand(ResolveBomRequest request) {
        return new ResolveBomCommand(
            Id.of(request.templateId()),
            Id.of(request.tenantId()),
            new DimensionsCm(request.widthCm(), request.heightCm()),
            safeOptions(request.optionSelections()),
            request.locale(),
            request.catalogVersion()
        );
    }

    private Map<String, String> safeOptions(Map<String, String> options) {
        return options == null ? Map.of() : options.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
            .collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().trim(),
                entry -> entry.getValue().trim()
            ));
    }

    private List<ValidationMessageDto> mapMessages(List<ValidationMessage> messages) {
        return messages.stream()
            .map(msg -> new ValidationMessageDto(msg.severity().name(), msg.code(), msg.message(), msg.attributeCode()))
            .toList();
    }
}
