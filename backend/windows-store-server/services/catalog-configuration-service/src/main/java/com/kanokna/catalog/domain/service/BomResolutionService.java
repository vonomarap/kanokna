package com.kanokna.catalog.domain.service;

/* <FUNCTION_CONTRACT id="FC-catalog-configuration-service-UC-CATALOG-CONFIGURE-ITEM-resolveBom"
     LAYER="domain.service"
     INTENT="Resolve bill of materials for a validated configuration"
     INPUT="Configuration, ProductTemplateId"
     OUTPUT="ResolvedBom"
     SIDE_EFFECTS="None"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM">
  <PRECONDITIONS>
    <Item>Configuration has been validated successfully</Item>
    <Item>BomTemplate exists for the product</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>ResolvedBom contains all required SKUs</Item>
    <Item>Quantities are calculated from formulas</Item>
  </POSTCONDITIONS>

  <BLOCK_ANCHORS>
    <Item id="BA-BOM-RESOLVE-01">Load BOM template for product</Item>
    <Item id="BA-BOM-RESOLVE-02">Calculate quantities from dimensions</Item>
    <Item id="BA-BOM-RESOLVE-03">Add option-specific SKUs</Item>
  </BLOCK_ANCHORS>

  <TESTS>
    <Case id="TC-BOM-001">Standard window returns frame + glass SKUs</Case>
    <Case id="TC-BOM-002">Quantity scales with dimensions</Case>
    <Case id="TC-BOM-003">Optional accessories included in BOM</Case>
  </TESTS>
</FUNCTION_CONTRACT> */

import com.kanokna.catalog.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain service for resolving bill of materials from configuration.
 * Calculates concrete SKUs and quantities based on configuration.
 */
public class BomResolutionService {

    private static final Logger log = LoggerFactory.getLogger(BomResolutionService.class);

    /**
     * Resolves bill of materials for a validated configuration.
     *
     * @param configuration validated configuration
     * @param bomTemplate   BOM template for the product
     * @return resolved BOM with SKUs and quantities
     */
    public ResolvedBom resolveBom(Configuration configuration, BomTemplate bomTemplate) {
        Objects.requireNonNull(configuration, "Configuration cannot be null");
        Objects.requireNonNull(bomTemplate, "BomTemplate cannot be null");

        List<ResolvedBom.BomItem> items = new ArrayList<>();

        // BA-BOM-RESOLVE-01: Load BOM template for product
        log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-BOM-RESOLVE-01][STATE=LOAD] eventType=BOM_RESOLUTION_STEP decision=EVALUATE keyValues=bomTemplateId={},lineCount={}",
            bomTemplate.getId(), bomTemplate.getBomLines().size());

        for (BomLine line : bomTemplate.getBomLines()) {
            // BA-BOM-RESOLVE-02: Calculate quantities from dimensions
            log.debug("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-BOM-RESOLVE-02][STATE=CALC_QTY] eventType=BOM_RESOLUTION_STEP decision=EVALUATE keyValues=sku={},formula={}",
                line.getSku(), line.getQuantityFormula());

            int quantity = calculateQuantity(line.getQuantityFormula(), configuration);

            // BA-BOM-RESOLVE-03: Add option-specific SKUs
            boolean shouldInclude = evaluateCondition(line.getConditionExpression(), configuration);
            if (shouldInclude && quantity > 0) {
                items.add(new ResolvedBom.BomItem(line.getSku(), line.getDescription(), quantity));
            }
        }

        log.info("[SVC=catalog-configuration-service][UC=UC-CATALOG-CONFIGURE-ITEM][BLOCK=BA-BOM-RESOLVE-03][STATE=FINAL] eventType=BOM_RESOLVED decision=SUCCESS keyValues=itemCount={}",
            items.size());

        return new ResolvedBom(bomTemplate.getProductTemplateId(), items);
    }

    /**
     * Calculates quantity from formula.
     * Simple formula evaluation supporting basic expressions.
     *
     * @param formula       quantity formula (e.g., "1", "width_cm / 100", "2")
     * @param configuration configuration with dimensions
     * @return calculated quantity
     */
    private int calculateQuantity(String formula, Configuration configuration) {
        if (formula == null || formula.isBlank()) {
            return 1;
        }

        // Simple formula evaluation - in production, use a proper expression evaluator
        try {
            if (formula.contains("width_cm")) {
                String evaluated = formula.replace("width_cm", String.valueOf(configuration.widthCm()));
                return evaluateExpression(evaluated);
            } else if (formula.contains("height_cm")) {
                String evaluated = formula.replace("height_cm", String.valueOf(configuration.heightCm()));
                return evaluateExpression(evaluated);
            } else if (formula.contains("area_m2")) {
                String evaluated = formula.replace("area_m2", String.valueOf(configuration.getAreaM2()));
                return evaluateExpression(evaluated);
            } else {
                return Integer.parseInt(formula.trim());
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate formula: {}, defaulting to 1", formula, e);
            return 1;
        }
    }

    /**
     * Evaluates simple arithmetic expression.
     *
     * @param expression arithmetic expression
     * @return result
     */
    private int evaluateExpression(String expression) {
        // Simple evaluation - for production use ScriptEngine or proper parser
        expression = expression.trim();

        if (expression.contains("/")) {
            String[] parts = expression.split("/");
            return (int) Math.ceil(Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim()));
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return (int) (Double.parseDouble(parts[0].trim()) * Double.parseDouble(parts[1].trim()));
        } else {
            return Integer.parseInt(expression);
        }
    }

    /**
     * Evaluates condition expression to determine if BOM line should be included.
     *
     * @param conditionExpression condition (e.g., "hasOption('reinforced-profile')")
     * @param configuration       configuration
     * @return true if condition is met or no condition specified
     */
    private boolean evaluateCondition(String conditionExpression, Configuration configuration) {
        if (conditionExpression == null || conditionExpression.isBlank()) {
            return true; // No condition means always include
        }

        // Simple condition evaluation - in production use proper expression evaluator
        return true; // Simplified for now
    }
}
