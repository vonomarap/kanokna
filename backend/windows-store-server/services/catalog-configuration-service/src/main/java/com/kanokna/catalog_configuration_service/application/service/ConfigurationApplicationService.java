package com.kanokna.catalog_configuration_service.application.service;

import com.kanokna.catalog_configuration_service.application.dto.ResolveBomCommand;
import com.kanokna.catalog_configuration_service.application.dto.ResolveBomResponse;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog_configuration_service.application.dto.ValidateConfigurationResponse;
import com.kanokna.catalog_configuration_service.application.port.in.QueryPort;
import com.kanokna.catalog_configuration_service.application.port.out.BomTemplateRepository;
import com.kanokna.catalog_configuration_service.application.port.out.OutboxPublisher;
import com.kanokna.catalog_configuration_service.application.port.out.ProductTemplateRepository;
import com.kanokna.catalog_configuration_service.domain.event.BomResolvedEvent;
import com.kanokna.catalog_configuration_service.domain.exception.CatalogDomainException;
import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.catalog_configuration_service.domain.model.ConfigurationSelection;
import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;
import com.kanokna.catalog_configuration_service.domain.model.ValidationResult;
import com.kanokna.catalog_configuration_service.domain.service.BomResolutionService;
import com.kanokna.catalog_configuration_service.domain.service.ConfigurationValidationService;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/* <MODULE_CONTRACT
    id="mod.catalog.application.validation-bom"
    ROLE="Application service orchestrating validation and BOM resolution use cases"
    SERVICE="catalog-configuration-service"
    LAYER="application"
    BOUNDED_CONTEXT="catalog-configuration"
    LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-VALIDATE-CONFIGURATION,backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-RESOLVE-BOM,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Validation,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Bom">
   <PURPOSE>
     Provide orchestrations for configuration validation and BOM resolution by delegating to domain services,
     persisting required aggregates, and publishing domain events via OutboxPublisher.
   </PURPOSE>
   <RESPONSIBILITIES>
     <Item>Load active templates and BOM templates for a tenant.</Item>
     <Item>Convert DTO commands into domain selections and invoke domain services.</Item>
     <Item>Publish domain events (ConfigurationValidatedEvent, BomResolvedEvent) through OutboxPublisher.</Item>
   </RESPONSIBILITIES>
   <CONTEXT>
     <UPSTREAM>
       - REST/GraphQL adapters call QueryPort.validateConfiguration/resolveBom.
     </UPSTREAM>
     <DOWNSTREAM>
       - ProductTemplateRepository for aggregate loading.
       - BomTemplateRepository for BOM mappings.
       - OutboxPublisher for event propagation.
     </DOWNSTREAM>
   </CONTEXT>
   <LOGGING>
     <Pattern>[APP][{useCase}][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="APP-VAL-LOAD" purpose="Load template and build selection"/>
       <Anchor id="APP-VAL-DOMAIN" purpose="Invoke domain validation"/>
       <Anchor id="APP-VAL-OUTBOX" purpose="Publish validation event when valid"/>
       <Anchor id="APP-BOM-LOAD" purpose="Load template/BOM templates"/>
       <Anchor id="APP-BOM-DOMAIN" purpose="Invoke domain BOM resolution"/>
       <Anchor id="APP-BOM-OUTBOX" purpose="Publish BOM resolved event"/>
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-APP-VAL-001">Valid configuration publishes event and returns valid=true with empty errors.</Case>
     <Case id="TC-APP-VAL-002">Unknown templateId raises CatalogDomainException.</Case>
     <Case id="TC-APP-BOM-001">Matching BOM template publishes event and returns merged items.</Case>
     <Case id="TC-APP-BOM-002">No BOM template triggers CatalogDomainException.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class ConfigurationApplicationService implements QueryPort {

    private final ProductTemplateRepository productTemplateRepository;
    private final BomTemplateRepository bomTemplateRepository;
    private final OutboxPublisher outboxPublisher;
    private final ConfigurationValidationService validationService;
    private final BomResolutionService bomResolutionService;

    public ConfigurationApplicationService(
        ProductTemplateRepository productTemplateRepository,
        BomTemplateRepository bomTemplateRepository,
        OutboxPublisher outboxPublisher,
        ConfigurationValidationService validationService,
        BomResolutionService bomResolutionService
    ) {
        this.productTemplateRepository = Objects.requireNonNull(productTemplateRepository, "productTemplateRepository");
        this.bomTemplateRepository = Objects.requireNonNull(bomTemplateRepository, "bomTemplateRepository");
        this.outboxPublisher = Objects.requireNonNull(outboxPublisher, "outboxPublisher");
        this.validationService = Objects.requireNonNull(validationService, "validationService");
        this.bomResolutionService = Objects.requireNonNull(bomResolutionService, "bomResolutionService");
    }

    /* <FUNCTION_CONTRACT
         id="validateConfiguration.app"
         LAYER="application.service"
         INTENT="Load template, validate selection via domain, and publish validation event"
         INPUT="ValidateConfigurationCommand"
         OUTPUT="ValidateConfigurationResponse"
         SIDE_EFFECTS="Publishes ConfigurationValidatedEvent when valid"
         LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-VALIDATE-CONFIGURATION;backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Validation">
       <PRECONDITIONS>
         <Item>Command fields are non-null and locale provided.</Item>
         <Item>Template exists and is ACTIVE for tenant.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>OutboxPublisher invoked when validation successful and event present.</Item>
         <Item>Response reflects domain ValidationResult (errors/warnings/traces).</Item>
       </POSTCONDITIONS>
       <ERROR_HANDLING>
         - CatalogDomainException when template not found or tenant mismatch.
       </ERROR_HANDLING>
       <LOGGING>
         - Adapters log DecisionTrace entries; anchors APP-VAL-LOAD/APP-VAL-DOMAIN/APP-VAL-OUTBOX are used for log state keys.
       </LOGGING>
       <TESTS>
         <Case id="TC-APP-VAL-001">Happy path publishes event and returns valid.</Case>
         <Case id="TC-APP-VAL-002">Missing template id -> CatalogDomainException.</Case>
       </TESTS>
     </FUNCTION_CONTRACT> */
    @Override
    public ValidateConfigurationResponse validateConfiguration(ValidateConfigurationCommand command) {
        ProductTemplate template = loadTemplate(command.templateId(), command.tenantId());

        /* <BLOCK_ANCHOR id="APP-VAL-LOAD" purpose="Build selection from command"/> */
        ConfigurationSelection selection = toSelection(command);

        /* <BLOCK_ANCHOR id="APP-VAL-DOMAIN" purpose="Delegate validation to domain service"/> */
        ValidationResult result = validationService.validateConfiguration(template, selection);

        /* <BLOCK_ANCHOR id="APP-VAL-OUTBOX" purpose="Publish validation event if valid"/> */
        result.validatedEvent().ifPresent(outboxPublisher::publish);

        return new ValidateConfigurationResponse(result.isValid(), result.errors(), result.warnings(), result.traces());
    }

    /* <FUNCTION_CONTRACT
         id="resolveBom.app"
         LAYER="application.service"
         INTENT="Resolve BOM via domain service and publish event"
         INPUT="ResolveBomCommand"
         OUTPUT="ResolveBomResponse"
         SIDE_EFFECTS="Publishes BomResolvedEvent when resolution succeeds"
         LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-RESOLVE-BOM;backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Flow-Bom">
       <PRECONDITIONS>
         <Item>Configuration already validated (enforced upstream).</Item>
         <Item>Template and BOM templates exist for tenant.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>OutboxPublisher invoked when event present.</Item>
         <Item>Duplicate SKUs merged per domain result.</Item>
       </POSTCONDITIONS>
       <ERROR_HANDLING>
         - CatalogDomainException when template missing or no BOM template matches.
       </ERROR_HANDLING>
       <LOGGING>
         - Anchors APP-BOM-LOAD/APP-BOM-DOMAIN/APP-BOM-OUTBOX used for adapter logs.
       </LOGGING>
       <TESTS>
         <Case id="TC-APP-BOM-001">Matching BOM template returns merged items and publishes event.</Case>
         <Case id="TC-APP-BOM-002">No matching template throws exception.</Case>
       </TESTS>
     </FUNCTION_CONTRACT> */
    @Override
    public ResolveBomResponse resolveBom(ResolveBomCommand command) {
        ProductTemplate template = loadTemplate(command.templateId(), command.tenantId());

        /* <BLOCK_ANCHOR id="APP-BOM-LOAD" purpose="Load BOM templates"/> */
        List<BomTemplate> bomTemplates = bomTemplateRepository.findByTemplate(command.templateId(), command.tenantId());
        if (bomTemplates.isEmpty()) {
            throw new CatalogDomainException("No BOM templates configured for template " + command.templateId().value());
        }

        /* <BLOCK_ANCHOR id="APP-BOM-DOMAIN" purpose="Delegate to domain BOM resolution"/> */
        BomTemplate.BomResolutionResult resolution = bomResolutionService.resolveBom(template, toSelection(command), bomTemplates);

        /* <BLOCK_ANCHOR id="APP-BOM-OUTBOX" purpose="Publish BomResolvedEvent"/> */
        resolution.bomResolvedEvent().ifPresent(outboxPublisher::publish);

        String bomCode = resolution.bomResolvedEvent().map(BomResolvedEvent::bomTemplateCode).orElse(null);
        return new ResolveBomResponse(template.id(), bomCode, resolution.items());
    }

    private ProductTemplate loadTemplate(Id templateId, Id tenantId) {
        return productTemplateRepository.findActiveById(templateId, tenantId)
            .orElseThrow(() -> new CatalogDomainException("Active template not found for tenant"));
    }

    private ConfigurationSelection toSelection(ValidateConfigurationCommand command) {
        return new ConfigurationSelection(
            command.templateId(),
            command.tenantId(),
            command.dimensions(),
            normalizeOptions(command.optionSelections()),
            command.locale(),
            command.catalogVersion()
        );
    }

    private ConfigurationSelection toSelection(ResolveBomCommand command) {
        return new ConfigurationSelection(
            command.templateId(),
            command.tenantId(),
            command.dimensions(),
            normalizeOptions(command.optionSelections()),
            command.locale(),
            command.catalogVersion()
        );
    }

    private Map<String, String> normalizeOptions(Map<String, String> selections) {
        return selections == null ? Map.of() : Map.copyOf(selections);
    }
}
