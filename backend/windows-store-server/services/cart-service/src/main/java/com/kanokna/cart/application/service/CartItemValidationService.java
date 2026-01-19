package com.kanokna.cart.application.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.service.dto.ValidationResult;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.ValidationStatus;

/**
 * MODULE_CONTRACT id="MC-cart-item-validation"
 * LAYER="application.service"
 * INTENT="Validate cart item configurations against catalog-configuration-service"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;DevelopmentPlan.xml#DP-SVC-cart-service"
 *
 * Service responsible for validating cart item configurations.
 * Delegates to catalog-configuration-service for actual validation logic.
 */
@Service
public class CartItemValidationService {

    private static final Logger log = LoggerFactory.getLogger(CartItemValidationService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE = "UC-CART-MANAGE";

    private final CatalogConfigurationPort catalogConfigurationPort;

    public CartItemValidationService(CatalogConfigurationPort catalogConfigurationPort) {
        this.catalogConfigurationPort = catalogConfigurationPort;
    }

    /**
     * FUNCTION_CONTRACT id="FC-cart-validation-validateConfiguration"
     * Validates a single configuration against the catalog service.
     *
     * @param snapshot the configuration snapshot to validate
     * @return validation result with status and any errors
     */
    public ValidationResult validateConfiguration(ConfigurationSnapshot snapshot) {
        log.atDebug()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-VALIDATE-01")
            .addKeyValue("state", "VALIDATE_CONFIG")
            .addKeyValue("productTemplateId", snapshot.productTemplateId())
            .log("Validating configuration");

        try {
            CatalogConfigurationPort.ValidationResult result = catalogConfigurationPort.validateConfiguration(snapshot);

            if (result == null || !result.available()) {
                log.atWarn()
                    .addKeyValue("svc", SERVICE)
                    .addKeyValue("uc", USE_CASE)
                    .addKeyValue("block", "BA-CART-VALIDATE-01")
                    .addKeyValue("state", "CATALOG_UNAVAILABLE")
                    .addKeyValue("productTemplateId", snapshot.productTemplateId())
                    .log("Catalog service unavailable for validation");
                return ValidationResult.unavailable();
            }

            if (result.valid()) {
                log.atDebug()
                    .addKeyValue("svc", SERVICE)
                    .addKeyValue("uc", USE_CASE)
                    .addKeyValue("block", "BA-CART-VALIDATE-01")
                    .addKeyValue("state", "VALID")
                    .addKeyValue("productTemplateId", snapshot.productTemplateId())
                    .log("Configuration is valid");
                return ValidationResult.validResult();
            }

            log.atDebug()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-VALIDATE-01")
                .addKeyValue("state", "INVALID")
                .addKeyValue("productTemplateId", snapshot.productTemplateId())
                .addKeyValue("errorCount", result.errors() != null ? result.errors().size() : 0)
                .log("Configuration is invalid");
            return ValidationResult.invalid(result.errors());

        } catch (Exception ex) {
            log.atError()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-VALIDATE-01")
                .addKeyValue("state", "ERROR")
                .addKeyValue("productTemplateId", snapshot.productTemplateId())
                .setCause(ex)
                .log("Error validating configuration");
            return ValidationResult.unavailable();
        }
    }

    /**
     * Revalidates all items in a cart.
     *
     * @param cart the cart to revalidate
     * @return revalidation summary with counts
     */
    public RevalidationSummary revalidateCartItems(Cart cart) {
        log.atDebug()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-REVALIDATE-01")
            .addKeyValue("state", "START")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("itemCount", cart.items().size())
            .log("Starting cart item revalidation");

        int validCount = 0;
        int invalidCount = 0;
        int unknownCount = 0;
        List<ItemValidationResult> results = new ArrayList<>();

        for (CartItem item : cart.items()) {
            ValidationResult result = validateConfiguration(item.configurationSnapshot());

            ValidationStatus status = result.status();
            String message = result.message();

            switch (status) {
                case VALID -> validCount++;
                case INVALID -> invalidCount++;
                case UNKNOWN -> unknownCount++;
            }

            results.add(new ItemValidationResult(
                item.itemId().toString(),
                status,
                message,
                result.errors()
            ));
        }

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-REVALIDATE-02")
            .addKeyValue("state", "COMPLETE")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("validCount", validCount)
            .addKeyValue("invalidCount", invalidCount)
            .addKeyValue("unknownCount", unknownCount)
            .log("Cart item revalidation complete");

        return new RevalidationSummary(validCount, invalidCount, unknownCount, results);
    }

    /**
     * Checks if cart has any invalid items.
     *
     * @param cart the cart to check
     * @return true if any items are invalid
     */
    public boolean hasInvalidItems(Cart cart) {
        for (CartItem item : cart.items()) {
            ValidationResult result = validateConfiguration(item.configurationSnapshot());
            if (!result.available() || !result.valid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts invalid items in cart.
     *
     * @param cart the cart to check
     * @return count of invalid items
     */
    public int countInvalidItems(Cart cart) {
        int count = 0;
        for (CartItem item : cart.items()) {
            ValidationResult result = validateConfiguration(item.configurationSnapshot());
            if (!result.available() || !result.valid()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Result of individual item validation.
     */
    public record ItemValidationResult(
        String itemId,
        ValidationStatus status,
        String message,
        List<String> errors
    ) {}

    /**
     * Summary of cart revalidation.
     */
    public record RevalidationSummary(
        int validCount,
        int invalidCount,
        int unknownCount,
        List<ItemValidationResult> results
    ) {
        public int totalCount() {
            return validCount + invalidCount + unknownCount;
        }

        public boolean allValid() {
            return invalidCount == 0 && unknownCount == 0;
        }
    }
}
