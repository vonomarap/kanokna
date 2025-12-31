package com.kanokna.catalog.domain.service;

/* <MODULE_CONTRACT id="MC-catalog-configuration-service-domain-ConfigurationValidation"
     ROLE="DomainService"
     SERVICE="catalog-configuration-service"
     LAYER="domain"
     BOUNDED_CONTEXT="catalog-configuration"
     SPECIFICATION="UC-CATALOG-CONFIGURE-ITEM">
  <PURPOSE>
    Domain service that validates product configurations against business rules.
    Ensures configurations are technically feasible and comply with all constraints.
  </PURPOSE>

  <RESPONSIBILITIES>
    <Item>Validate dimensions against product constraints (50-400cm)</Item>
    <Item>Check material/glazing compatibility</Item>
    <Item>Verify option dependencies are satisfied</Item>
    <Item>Detect and report exclusion rule violations</Item>
    <Item>Return detailed validation errors for UI feedback</Item>
  </RESPONSIBILITIES>

  <INVARIANTS>
    <Item>Validation is pure (no side effects, no state mutation)</Item>
    <Item>Validation is deterministic for same inputs and rule version</Item>
    <Item>All rules must be evaluated; partial results are not returned</Item>
    <Item>ValidationResult.valid == true iff errors list is empty</Item>
  </INVARIANTS>

  <CONTEXT>
    <UPSTREAM>
      <Item>ConfigurationValidationUseCaseService: orchestrates validation + pricing</Item>
      <Item>CatalogConfigurationGrpcService: exposes validation to other services</Item>
    </UPSTREAM>
    <DOWNSTREAM>
      <Item>ConfigurationRuleSetRepository: loads rules for product</Item>
      <Item>ProductTemplateRepository: loads product constraints</Item>
    </DOWNSTREAM>
  </CONTEXT>

  <PUBLIC_API>
    <Method name="validate" input="Configuration, ProductTemplateId" output="ValidationResult"/>
  </PUBLIC_API>

  <BUSINESS_RULES>
    <Rule id="BR-CFG-001">Dimensions: 50cm <= width,height <= 400cm</Rule>
    <Rule id="BR-CFG-002">Glazing thickness depends on frame material strength</Rule>
    <Rule id="BR-CFG-003">Some lamination colors only available with specific materials</Rule>
    <Rule id="BR-CFG-004">Large sizes (>250cm) require reinforced profiles</Rule>
  </BUSINESS_RULES>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="ERR-CFG-DIMENSIONS">Dimensions outside allowed range</Item>
    <Item type="BUSINESS" code="ERR-CFG-MATERIAL-GLAZING">Incompatible material/glazing combination</Item>
    <Item type="BUSINESS" code="ERR-CFG-DEPENDENCY">Required dependent option not selected</Item>
    <Item type="BUSINESS" code="ERR-CFG-EXCLUSION">Mutually exclusive options selected</Item>
    <Item type="TECHNICAL" code="ERR-RULES-NOT-FOUND">Rule set not found for product</Item>
  </ERROR_HANDLING>

  <LOGGING>
    <FORMAT>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=...][STATE=...] eventType=... decision=... keyValues=...</FORMAT>
    <EXAMPLES>
      <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-01][STATE=CHECK_SIZE] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE keyValues=productId,width_cm=120,height_cm=150</Item>
      <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-99][STATE=FINAL] eventType=CONFIG_VALIDATION_RESULT decision=ACCEPT keyValues=valid=true,errors_count=0</Item>
    </EXAMPLES>
  </LOGGING>

  <TESTS>
    <Case id="TC-VAL-001">Valid configuration with all constraints satisfied returns valid=true</Case>
    <Case id="TC-VAL-002">Width below 50cm returns ERR-CFG-DIMENSIONS</Case>
    <Case id="TC-VAL-003">Width above 400cm returns ERR-CFG-DIMENSIONS</Case>
    <Case id="TC-VAL-004">Incompatible material/glazing returns ERR-CFG-MATERIAL-GLAZING</Case>
    <Case id="TC-VAL-005">Missing dependent option returns ERR-CFG-DEPENDENCY</Case>
    <Case id="TC-VAL-006">Exclusive options together returns ERR-CFG-EXCLUSION</Case>
    <Case id="TC-VAL-007">Large size auto-applies reinforcement rule</Case>
    <Case id="TC-VAL-008">Missing ruleset returns ERR-RULES-NOT-FOUND</Case>
  </TESTS>

  <LINKS>
    <Link ref="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM"/>
    <Link ref="DevelopmentPlan.xml#DP-SVC-catalog-configuration-service"/>
    <Link ref="DevelopmentPlan.xml#Flow-Config-Pricing"/>
  </LINKS>
</MODULE_CONTRACT> */

/* <FUNCTION_CONTRACT id="FC-catalog-configuration-service-UC-CATALOG-CONFIGURE-ITEM-validateConfiguration"
     LAYER="domain.service"
     INTENT="Validate a product configuration against all business rules"
     INPUT="Configuration, ProductTemplateId"
     OUTPUT="ValidationResult"
     SIDE_EFFECTS="None"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM;DevelopmentPlan.xml#Flow-Config-Pricing">
  <PRECONDITIONS>
    <Item>configuration != null</Item>
    <Item>productTemplateId != null</Item>
    <Item>configuration.dimensions are within domain range</Item>
    <Item>configuration.selectedOptions has at least required option groups</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>ValidationResult is never null</Item>
    <Item>ValidationResult.valid == true iff errors is empty</Item>
    <Item>Each error has unique code and field reference</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Validation is deterministic for same inputs and rule version</Item>
    <Item>No database writes during validation</Item>
    <Item>No external service calls during validation</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="BUSINESS" code="ERR-CFG-DIMENSIONS">width/height outside [50, 400] cm</Item>
    <Item type="BUSINESS" code="ERR-CFG-MATERIAL-GLAZING">Incompatible combination</Item>
    <Item type="BUSINESS" code="ERR-CFG-DEPENDENCY">Missing required dependent option</Item>
    <Item type="BUSINESS" code="ERR-CFG-EXCLUSION">Mutually exclusive options selected</Item>
    <Item type="TECHNICAL" code="ERR-RULES-NOT-FOUND">Rule set not found</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-CFG-VAL-01">Check dimension constraints</Item>
    <Item id="BA-CFG-VAL-02">Check material/glazing compatibility</Item>
    <Item id="BA-CFG-VAL-03">Check option dependencies</Item>
    <Item id="BA-CFG-VAL-04">Check exclusion rules</Item>
    <Item id="BA-CFG-VAL-99">Final validation result</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-01][STATE=CHECK_SIZE] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE keyValues=productId,width_cm,height_cm</Item>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-02][STATE=CHECK_COMPAT] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE keyValues=material,glazingType</Item>
    <Item>[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-99][STATE=FINAL] eventType=CONFIG_VALIDATION_RESULT decision=ACCEPT|REJECT keyValues=valid,errors_count</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-FUNC-VAL-001">Valid config returns valid=true and empty errors</Case>
    <Case id="TC-FUNC-VAL-002">Invalid dimensions returns ERR-CFG-DIMENSIONS with field path</Case>
    <Case id="TC-FUNC-VAL-003">Incompatible options returns ERR-CFG-MATERIAL-GLAZING</Case>
    <Case id="TC-FUNC-VAL-004">Multiple errors are aggregated in result</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

import com.kanokna.catalog.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain service for validating product configurations.
 * Pure, stateless validation logic with no framework dependencies.
 */
public class ConfigurationValidationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationValidationService.class);

    private final RuleEvaluator ruleEvaluator;

    public ConfigurationValidationService(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = Objects.requireNonNull(ruleEvaluator, "RuleEvaluator cannot be null");
    }

    /**
     * Validates a configuration against product constraints and business rules.
     *
     * @param configuration    customer configuration with dimensions and selected options
     * @param productTemplate  product template with dimension constraints
     * @param ruleSet          active rule set for the product
     * @return validation result with errors (if any)
     */
    public ValidationResult validate(
        Configuration configuration,
        ProductTemplate productTemplate,
        ConfigurationRuleSet ruleSet
    ) {
        Objects.requireNonNull(configuration, "Configuration cannot be null");
        Objects.requireNonNull(productTemplate, "ProductTemplate cannot be null");

        List<ValidationError> errors = new ArrayList<>();

        // BA-CFG-VAL-01: Check dimension constraints
        log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-01][STATE=CHECK_SIZE] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE keyValues=productId={},width_cm={},height_cm={}",
            productTemplate.getId(), configuration.widthCm(), configuration.heightCm());

        DimensionConstraints constraints = productTemplate.getDimensionConstraints();
        if (!constraints.allows(configuration.widthCm(), configuration.heightCm())) {
            errors.add(ValidationError.of(
                "ERR-CFG-DIMENSIONS",
                String.format("Dimensions (%d x %d cm) outside allowed range [%d-%d cm width, %d-%d cm height]",
                    configuration.widthCm(), configuration.heightCm(),
                    constraints.minWidthCm(), constraints.maxWidthCm(),
                    constraints.minHeightCm(), constraints.maxHeightCm()),
                "dimensions"
            ));
        }

        if (ruleSet != null) {
            // BA-CFG-VAL-02: Check material/glazing compatibility
            log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-02][STATE=CHECK_COMPAT] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE keyValues=ruleCount={}",
                ruleSet.getActiveRules().size());

            errors.addAll(ruleEvaluator.evaluateCompatibilityRules(configuration, ruleSet));

            // BA-CFG-VAL-03: Check option dependencies
            log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-03][STATE=CHECK_DEPS] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE");
            errors.addAll(ruleEvaluator.evaluateDependencyRules(configuration, ruleSet));

            // BA-CFG-VAL-04: Check exclusion rules
            log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-04][STATE=CHECK_EXCL] eventType=CONFIG_VALIDATION_STEP decision=EVALUATE");
            errors.addAll(ruleEvaluator.evaluateExclusionRules(configuration, ruleSet));
        }

        // BA-CFG-VAL-99: Final validation result
        boolean valid = errors.isEmpty();
        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-CFG-VAL-99][STATE=FINAL] eventType=CONFIG_VALIDATION_RESULT decision={} keyValues=valid={},errors_count={}",
            valid ? "ACCEPT" : "REJECT", valid, errors.size());

        return valid ? ValidationResult.success() : ValidationResult.failure(errors);
    }
}
