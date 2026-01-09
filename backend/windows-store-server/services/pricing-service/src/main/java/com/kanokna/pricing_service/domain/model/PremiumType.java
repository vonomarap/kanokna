package com.kanokna.pricing_service.domain.model;

/**
 * Type of option premium per DEC-PRICING-PREMIUM-TYPES.
 */
public enum PremiumType {
    /** Fixed monetary amount added to base price */
    ABSOLUTE,

    /** Percentage of base price */
    PERCENTAGE
}
