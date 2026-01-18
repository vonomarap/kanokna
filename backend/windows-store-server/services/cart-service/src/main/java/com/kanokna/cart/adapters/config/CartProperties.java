package com.kanokna.cart.adapters.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import com.kanokna.shared.money.Currency;

/**
 * MODULE_CONTRACT id="MC-cart-properties"
 * LAYER="adapters.config"
 * INTENT="Externalize all cart-service configuration; eliminate magic numbers"
 * LINKS="RequirementsAnalysis.xml#BR-CART-015;RequirementsAnalysis.xml#BR-CART-028"
 *
 * Configuration properties for cart-service using immutable record pattern.
 * All configurable values are externalized here to eliminate magic numbers
 * and allow environment-specific tuning, including defaults like currency
 * and allowed product families.
 */
@Validated
@ConfigurationProperties(prefix = "kanokna.cart")
public record CartProperties(
    @Valid @NotNull Timeouts timeouts,
    @Valid @NotNull Limits limits,
    @Valid @NotNull Behavior behavior,
    @Valid @NotNull Defaults defaults
) {
    /**
     * Compact constructor providing null-safe defaults.
     */
    public CartProperties {
        timeouts = timeouts != null ? timeouts : new Timeouts(
            Duration.ofDays(7),      // anonymousTtl (BR-CART-SESSION-TTL)
            Duration.ofHours(72),    // abandonedThreshold (BR-CART-ABANDONED)
            Duration.ofMinutes(15),  // snapshotValidity (BR-CART-028)
            Duration.ofMinutes(30),  // priceQuoteStaleness
            Duration.ofSeconds(5),   // catalogValidationTimeout
            Duration.ofSeconds(5)    // pricingQuoteTimeout
        );
        limits = limits != null ? limits : new Limits(50, 100);
        behavior = behavior != null ? behavior : new Behavior(1.0, false,
            List.of("WINDOW", "DOOR", "ACCESSORY"));
        defaults = defaults != null ? defaults : new Defaults(Currency.RUB);
    }

    /**
     * Timeout-related configuration values.
     */
    public record Timeouts(
        /** TTL for anonymous carts. Default: 7 days */
        @NotNull Duration anonymousTtl,
        /** Threshold for abandoned cart detection. Default: 72 hours */
        @NotNull Duration abandonedThreshold,
        /** Validity of cart snapshot for checkout. Default: 15 minutes */
        @NotNull Duration snapshotValidity,
        /** Duration after which price quotes are stale. Default: 30 minutes */
        @NotNull Duration priceQuoteStaleness,
        /** Timeout for catalog validation calls. Default: 5 seconds */
        @NotNull Duration catalogValidationTimeout,
        /** Timeout for pricing quote calls. Default: 5 seconds */
        @NotNull Duration pricingQuoteTimeout
    ) {}

    /**
     * Cart limits configuration.
     */
    public record Limits(
        /** Maximum items allowed in a single cart. Default: 50 */
        @Positive int maxItemsPerCart,
        /** Maximum quantity per line item. Default: 100 */
        @Positive int maxQuantityPerItem
    ) {}

    /**
     * Cart behavior configuration.
     */
    public record Behavior(
        /** Price change threshold % for user notification. Default: 1.0 */     
        @PositiveOrZero double priceChangeThresholdPercent,
        /** Auto-refresh prices on cart retrieval. Default: false */
        boolean autoRefreshPrices,
        /** Allowed product families for cart items. Default: WINDOW, DOOR, ACCESSORY */
        @NotNull List<String> allowedProductFamilies
    ) {}

    /**
     * Default values for cart creation.
     */
    public record Defaults(
        /** Default currency for new carts. Default: RUB */
        @NotNull Currency defaultCurrency
    ) {}
}
