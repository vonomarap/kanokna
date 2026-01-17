package com.kanokna.cart.application.service;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.service.dto.CheckoutValidationResult;
import com.kanokna.cart.application.service.dto.PriceRefreshResult;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartSnapshot;
import com.kanokna.cart.domain.model.SnapshotId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE_CONTRACT id="MC-cart-checkout"
 * LAYER="application.service"
 * INTENT="Handle checkout preparation: validation, snapshot creation, price acknowledgment"
 * LINKS="RequirementsAnalysis.xml#UC-ORDER-PLACE;DevelopmentPlan.xml#Flow-Checkout-Payment"
 *
 * Service responsible for checkout-related operations.
 * Validates cart state and creates immutable snapshots for order creation.
 */
@Service
public class CartCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CartCheckoutService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE = "UC-ORDER-PLACE";

    private final CartItemValidationService validationService;
    private final CartPricingService pricingService;
    private final CartProperties properties;

    public CartCheckoutService(
            CartItemValidationService validationService,
            CartPricingService pricingService,
            CartProperties properties) {
        this.validationService = validationService;
        this.pricingService = pricingService;
        this.properties = properties;
    }

    /**
     * FUNCTION_CONTRACT id="FC-cart-checkout-createSnapshot"
     * Creates an immutable snapshot of the cart for checkout.
     *
     * @param cart the cart to snapshot
     * @param acknowledgePriceChanges whether user has acknowledged price changes
     * @return the created snapshot
     */
    public SnapshotCreationResult createSnapshot(Cart cart, boolean acknowledgePriceChanges) {
        // BA-CART-SNAP-01: Load cart and validate not empty
        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-SNAP-01")
            .addKeyValue("state", "START")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("itemCount", cart.items().size())
            .log("Starting snapshot creation");

        // BA-CART-SNAP-02: Validate all configurations
        CheckoutValidationResult validationResult = validateForCheckout(cart);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-SNAP-02")
            .addKeyValue("state", "VALIDATED")
            .addKeyValue("validCount", validationResult.validItemCount())
            .addKeyValue("invalidCount", validationResult.invalidItemCount())
            .log("Cart validation complete");

        if (!validationResult.valid() && validationResult.invalidItemCount() > 0) {
            return SnapshotCreationResult.invalidItems(validationResult);
        }

        // BA-CART-SNAP-03: Refresh all prices
        PriceRefreshResult priceResult = pricingService.refreshAllPrices(cart);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-SNAP-03")
            .addKeyValue("state", "PRICES_REFRESHED")
            .addKeyValue("itemsUpdated", priceResult.itemsUpdated())
            .addKeyValue("totalChanged", priceResult.totalChanged())
            .addKeyValue("changePercent", priceResult.changePercent())
            .log("Prices refreshed for snapshot");

        if (priceResult.hasFailures()) {
            return SnapshotCreationResult.pricingFailed(priceResult);
        }

        // Check if price change requires acknowledgment
        if (requiresAcknowledgement(priceResult) && !acknowledgePriceChanges) {
            log.atInfo()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-SNAP-03")
                .addKeyValue("state", "REQUIRES_ACKNOWLEDGMENT")
                .addKeyValue("changePercent", priceResult.changePercent())
                .log("Price change requires user acknowledgment");
            return SnapshotCreationResult.requiresAcknowledgment(priceResult);
        }

        // BA-CART-SNAP-04: Create immutable snapshot
        Duration validity = properties.getSnapshotValidity() != null
            ? properties.getSnapshotValidity()
            : Duration.ofMinutes(15);

        Instant now = Instant.now();
        CartSnapshot snapshot = cart.createSnapshot(SnapshotId.generate(), validity, now);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-SNAP-04")
            .addKeyValue("state", "SNAPSHOT_CREATED")
            .addKeyValue("snapshotId", snapshot.snapshotId().toString())
            .addKeyValue("total", snapshot.totals().total())
            .addKeyValue("validUntil", snapshot.validUntil())
            .log("Snapshot created successfully");

        return SnapshotCreationResult.success(snapshot, priceResult);
    }

    /**
     * Validates cart for checkout readiness.
     *
     * @param cart the cart to validate
     * @return validation result
     */
    public CheckoutValidationResult validateForCheckout(Cart cart) {
        log.atDebug()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-VALIDATE-CHECKOUT")
            .addKeyValue("state", "START")
            .addKeyValue("cartId", cart.cartId().toString())
            .log("Validating cart for checkout");

        int validCount = 0;
        int invalidCount = 0;
        List<String> invalidItemIds = new ArrayList<>();

        for (CartItem item : cart.items()) {
            var result = validationService.validateConfiguration(item.configurationSnapshot());
            if (result.available() && result.valid()) {
                validCount++;
            } else {
                invalidCount++;
                invalidItemIds.add(item.itemId().toString());
            }
        }

        if (invalidCount > 0) {
            return CheckoutValidationResult.invalidItems(validCount, invalidCount, invalidItemIds);
        }

        return CheckoutValidationResult.valid(validCount);
    }

    /**
     * Checks if price change requires user acknowledgment.
     *
     * @param priceResult the price refresh result
     * @return true if acknowledgment is required
     */
    public boolean requiresAcknowledgement(PriceRefreshResult priceResult) {
        if (!priceResult.totalChanged()) {
            return false;
        }
        return priceResult.changePercent() > properties.getPriceChangeThresholdPercent();
    }

    /**
     * Result of snapshot creation operation.
     */
    public record SnapshotCreationResult(
        boolean success,
        CartSnapshot snapshot,
        PriceRefreshResult priceResult,
        CheckoutValidationResult validationResult,
        FailureReason failureReason
    ) {
        public enum FailureReason {
            NONE,
            INVALID_ITEMS,
            PRICING_FAILED,
            REQUIRES_ACKNOWLEDGMENT
        }

        public static SnapshotCreationResult success(CartSnapshot snapshot, PriceRefreshResult priceResult) {
            return new SnapshotCreationResult(true, snapshot, priceResult, null, FailureReason.NONE);
        }

        public static SnapshotCreationResult invalidItems(CheckoutValidationResult validationResult) {
            return new SnapshotCreationResult(false, null, null, validationResult, FailureReason.INVALID_ITEMS);
        }

        public static SnapshotCreationResult pricingFailed(PriceRefreshResult priceResult) {
            return new SnapshotCreationResult(false, null, priceResult, null, FailureReason.PRICING_FAILED);
        }

        public static SnapshotCreationResult requiresAcknowledgment(PriceRefreshResult priceResult) {
            return new SnapshotCreationResult(false, null, priceResult, null, FailureReason.REQUIRES_ACKNOWLEDGMENT);
        }
    }
}
