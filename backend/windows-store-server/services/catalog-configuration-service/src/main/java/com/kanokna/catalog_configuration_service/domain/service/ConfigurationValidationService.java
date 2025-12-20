package com.kanokna.catalog_configuration_service.domain.service;

import com.kanokna.catalog_configuration_service.domain.event.ConfigurationValidatedEvent;
import com.kanokna.catalog_configuration_service.domain.exception.CatalogDomainException;
import com.kanokna.catalog_configuration_service.domain.model.ConfigurationRuleSet;
import com.kanokna.catalog_configuration_service.domain.model.ConfigurationSelection;
import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;
import com.kanokna.catalog_configuration_service.domain.model.RuleEvaluation;
import com.kanokna.catalog_configuration_service.domain.model.RuleEffect;
import com.kanokna.catalog_configuration_service.domain.model.ValidationResult;
import com.kanokna.shared.measure.DimensionsCm;

import java.util.Map;
import java.util.Objects;

/* <MODULE_CONTRACT id="mod.catalog.domain.validation"
     ROLE="Domain service for configuration validation"
     SERVICE="catalog-configuration-service" LAYER="domain.service"
     BOUNDED_CONTEXT="catalog-configuration"
     LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-VALIDATE-CONFIGURATION,backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-validation">
   <PURPOSE>
     Validate window/door configurations against template structure, dimensional policy, and compatibility rules
     while emitting belief-state traces for adapters to log and for outbox event creation.
   </PURPOSE>
   <RESPONSIBILITIES>
     <Item>Verify template/tenant/cross-version alignment for incoming configuration selections.</Item>
     <Item>Enforce dimensional policy bounds and required attribute selections.</Item>
     <Item>Evaluate compatibility rules (deny/warn) and aggregate errors deterministically.</Item>
     <Item>Create ConfigurationValidatedEvent when validation passes.</Item>
   </RESPONSIBILITIES>
   <INVARIANTS>
     <Item>No mutation of aggregates; service is pure and side-effect free.</Item>
     <Item>ValidationResult.valid == true only when no errors are recorded.</Item>
   </INVARIANTS>
   <LOGGING>
     <Pattern>[CFG][validateConfiguration][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="CFG-VAL-RANGE" purpose="Check dimensional ranges"/>
       <Anchor id="CFG-VAL-COMPAT" purpose="Evaluate required attributes and rule compatibility"/>
       <Anchor id="CFG-VAL-RESULT" purpose="Aggregate result and emit event if valid"/>
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-CFG-VAL-001">Valid selection with all required attributes and dimensions produces validated event.</Case>
     <Case id="TC-CFG-VAL-002">Missing required attribute returns error for attribute group.</Case>
     <Case id="TC-CFG-VAL-003">Out-of-range dimensions return CFG-VAL-RANGE error.</Case>
     <Case id="TC-CFG-VAL-004">Compatibility deny rule adds blocking error; warn rule adds warning.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class ConfigurationValidationService {

    /* <FUNCTION_CONTRACT id="validateConfiguration"
         LAYER="domain.service"
         INTENT="Validate a configuration selection for a product template"
         INPUT="ProductTemplate template, ConfigurationSelection selection"
         OUTPUT="ValidationResult"
         SIDE_EFFECTS="None; emits ConfigurationValidatedEvent in result when valid."
         LINKS="backend/windows-store-server/services/catalog-configuration-service/docs/RequirementsAnalysis.xml#UC-VALIDATE-CONFIGURATION;backend/windows-store-server/services/catalog-configuration-service/docs/DevelopmentPlan.xml#Contracts-domain-validation">
       <PRECONDITIONS>
         <Item>template and selection are non-null and share the same templateId and tenantId.</Item>
         <Item>Selection contains locale and dimensions.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>ValidationResult.valid == true only if no errors recorded.</Item>
         <Item>On valid result, ConfigurationValidatedEvent is attached with signatureKey.</Item>
       </POSTCONDITIONS>
       <INVARIANTS>
         <Item>Domain entities remain immutable; no external side effects.</Item>
       </INVARIANTS>
       <ERROR_HANDLING>
         - Throws CatalogDomainException for template/tenant mismatches.
         - Records ValidationMessage ERROR for dimension or rule violations.
       </ERROR_HANDLING>
       <LOGGING>
         - Emit DecisionTrace entries following pattern [CFG][validateConfiguration][block=ID][state=STATE] for adapters to log.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    public ValidationResult validateConfiguration(ProductTemplate template, ConfigurationSelection selection) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(selection, "selection");

        if (!template.id().equals(selection.templateId())) {
            throw new CatalogDomainException("Selection references unknown template: " + selection.templateId());
        }
        if (!template.tenantId().equals(selection.tenantId())) {
            throw new CatalogDomainException("Tenant mismatch between template and selection");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        /* <BLOCK_ANCHOR id="CFG-VAL-RANGE" purpose="Check dimensional ranges"/> */
        builder.trace("CFG-VAL-RANGE", "CHECK_DIMENSIONS",
            "[CFG][validateConfiguration][block=CFG-VAL-RANGE][state=CHECK_DIMENSIONS] dimensions=" + describeDimensions(selection.dimensions()));
        template.dimensionPolicy()
            .violationMessage(selection.dimensions())
            .ifPresent(message -> builder.addError("DIMENSION_RANGE", message, "dimensions"));

        /* <BLOCK_ANCHOR id="CFG-VAL-COMPAT" purpose="Evaluate required attributes and rule compatibility"/> */
        enforceRequiredAttributes(template, selection, builder);
        enforceOptionExistence(template, selection, builder);
        evaluateRules(template.ruleSet(), selection, builder);

        /* <BLOCK_ANCHOR id="CFG-VAL-RESULT" purpose="Aggregate errors/warnings and emit event"/> */
        if (builder.build().isValid()) {
            builder.validatedEvent(ConfigurationValidatedEvent.of(
                selection.templateId(),
                selection.tenantId(),
                selection.catalogVersion(),
                selection.signatureKey()
            ));
            builder.trace("CFG-VAL-RESULT", "VALID",
                "[CFG][validateConfiguration][block=CFG-VAL-RESULT][state=VALID] signature=" + selection.signatureKey());
        } else {
            builder.trace("CFG-VAL-RESULT", "INVALID",
                "[CFG][validateConfiguration][block=CFG-VAL-RESULT][state=INVALID] errors=" + builder.build().errors().size());
        }

        return builder.build();
    }

    private void enforceRequiredAttributes(ProductTemplate template, ConfigurationSelection selection, ValidationResult.Builder builder) {
        for (ProductTemplate.AttributeGroup group : template.attributeGroups()) {
            if (group.required() && !selection.hasSelection(group.code())) {
                builder.addError("ATTRIBUTE_REQUIRED",
                    "Attribute group %s is required".formatted(group.code()),
                    group.code());
            }
        }
    }

    private void enforceOptionExistence(ProductTemplate template, ConfigurationSelection selection, ValidationResult.Builder builder) {
        for (Map.Entry<String, String> entry : selection.optionSelections().entrySet()) {
            if (!template.hasAttribute(entry.getKey())) {
                builder.addError("ATTRIBUTE_UNKNOWN",
                    "Attribute %s is not part of template".formatted(entry.getKey()),
                    entry.getKey());
                continue;
            }
            ProductTemplate.AttributeGroup group = template.attributeGroup(entry.getKey());
            boolean optionExists = group.options().stream()
                .anyMatch(option -> option.code().equals(entry.getValue()) && !option.deprecated());
            if (!optionExists) {
                builder.addError("OPTION_INVALID",
                    "Option %s is not allowed for attribute %s".formatted(entry.getValue(), entry.getKey()),
                    entry.getKey());
            }
        }
    }

    private void evaluateRules(ConfigurationRuleSet ruleSet, ConfigurationSelection selection, ValidationResult.Builder builder) {
        for (RuleEvaluation evaluation : ruleSet.evaluate(selection)) {
            if (evaluation.effect() == RuleEffect.DENY) {
                builder.addError(evaluation.ruleCode(), evaluation.message(), evaluation.attributeCode());
                builder.trace("CFG-VAL-COMPAT", "RULE_DENY",
                    "[CFG][validateConfiguration][block=CFG-VAL-COMPAT][state=RULE_DENY] rule=%s".formatted(evaluation.ruleCode()));
            } else if (evaluation.effect() == RuleEffect.WARN) {
                builder.addWarning(evaluation.ruleCode(), evaluation.message(), evaluation.attributeCode());
                builder.trace("CFG-VAL-COMPAT", "RULE_WARN",
                    "[CFG][validateConfiguration][block=CFG-VAL-COMPAT][state=RULE_WARN] rule=%s".formatted(evaluation.ruleCode()));
            }
        }
    }

    private String describeDimensions(DimensionsCm dimensions) {
        return "%d x %d cm".formatted(dimensions.width(), dimensions.height());
    }
}
