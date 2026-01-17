package com.kanokna.cart.adapters.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MODULE_CONTRACT id="MC-cart-properties"
 * LAYER="adapters.config"
 * INTENT="Externalize all cart-service configuration; eliminate magic numbers"
 * LINKS="RequirementsAnalysis.xml#BR-CART-015;RequirementsAnalysis.xml#BR-CART-028"
 *
 * Configuration properties for cart-service.
 * All configurable values are externalized here to eliminate magic numbers
 * and allow environment-specific tuning.
 */
@ConfigurationProperties(prefix = "kanokna.cart")
public class CartProperties {

    /**
     * Time-to-live for anonymous (session-based) carts before cleanup.
     * Default: 7 days (BR-CART-SESSION-TTL)
     */
    private Duration anonymousTtl = Duration.ofDays(7);

    /**
     * Threshold after which a cart is considered abandoned.
     * Default: 72 hours (BR-CART-ABANDONED)
     */
    private Duration abandonedThreshold = Duration.ofHours(72);

    /**
     * Validity duration for cart snapshots used in checkout.
     * After this period, snapshot expires and checkout must restart.
     * Default: 15 minutes (BR-CART-028)
     */
    private Duration snapshotValidity = Duration.ofMinutes(15);

    /**
     * Price change threshold percentage for user notification/acknowledgment.
     * If total price changes by more than this percentage during checkout,
     * user must acknowledge before proceeding.
     * Default: 1.0% (BR-CART-015)
     */
    private double priceChangeThresholdPercent = 1.0;

    /**
     * Maximum number of items allowed in a single cart.
     * Default: 50 items
     */
    private int maxItemsPerCart = 50;

    /**
     * Maximum quantity allowed for a single line item.
     * Default: 100 units
     */
    private int maxQuantityPerItem = 100;

    /**
     * Duration after which price quotes are considered stale.
     * Default: 30 minutes
     */
    private Duration priceQuoteStaleness = Duration.ofMinutes(30);

    /**
     * Whether to auto-refresh prices when retrieving cart.
     * Default: false (lazy refresh on explicit action)
     */
    private boolean autoRefreshPrices = false;

    /**
     * Timeout for catalog service validation calls.
     * Default: 5 seconds
     */
    private Duration catalogValidationTimeout = Duration.ofSeconds(5);

    /**
     * Timeout for pricing service quote calls.
     * Default: 5 seconds
     */
    private Duration pricingQuoteTimeout = Duration.ofSeconds(5);

    // Getters and Setters

    public Duration getAnonymousTtl() {
        return anonymousTtl;
    }

    public void setAnonymousTtl(Duration anonymousTtl) {
        this.anonymousTtl = anonymousTtl;
    }

    public Duration getAbandonedThreshold() {
        return abandonedThreshold;
    }

    public void setAbandonedThreshold(Duration abandonedThreshold) {
        this.abandonedThreshold = abandonedThreshold;
    }

    public Duration getSnapshotValidity() {
        return snapshotValidity;
    }

    public void setSnapshotValidity(Duration snapshotValidity) {
        this.snapshotValidity = snapshotValidity;
    }

    public double getPriceChangeThresholdPercent() {
        return priceChangeThresholdPercent;
    }

    public void setPriceChangeThresholdPercent(double priceChangeThresholdPercent) {
        this.priceChangeThresholdPercent = priceChangeThresholdPercent;
    }

    public int getMaxItemsPerCart() {
        return maxItemsPerCart;
    }

    public void setMaxItemsPerCart(int maxItemsPerCart) {
        this.maxItemsPerCart = maxItemsPerCart;
    }

    public int getMaxQuantityPerItem() {
        return maxQuantityPerItem;
    }

    public void setMaxQuantityPerItem(int maxQuantityPerItem) {
        this.maxQuantityPerItem = maxQuantityPerItem;
    }

    public Duration getPriceQuoteStaleness() {
        return priceQuoteStaleness;
    }

    public void setPriceQuoteStaleness(Duration priceQuoteStaleness) {
        this.priceQuoteStaleness = priceQuoteStaleness;
    }

    public boolean isAutoRefreshPrices() {
        return autoRefreshPrices;
    }

    public void setAutoRefreshPrices(boolean autoRefreshPrices) {
        this.autoRefreshPrices = autoRefreshPrices;
    }

    public Duration getCatalogValidationTimeout() {
        return catalogValidationTimeout;
    }

    public void setCatalogValidationTimeout(Duration catalogValidationTimeout) {
        this.catalogValidationTimeout = catalogValidationTimeout;
    }

    public Duration getPricingQuoteTimeout() {
        return pricingQuoteTimeout;
    }

    public void setPricingQuoteTimeout(Duration pricingQuoteTimeout) {
        this.pricingQuoteTimeout = pricingQuoteTimeout;
    }
}
