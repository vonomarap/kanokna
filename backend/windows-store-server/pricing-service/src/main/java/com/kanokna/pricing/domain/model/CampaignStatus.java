package com.kanokna.pricing.domain.model;

/**
 * Status of a promotional campaign.
 */
public enum CampaignStatus {
    /** Campaign scheduled for future activation */
    SCHEDULED,

    /** Campaign is currently active */
    ACTIVE,

    /** Campaign has expired */
    EXPIRED,

    /** Campaign was manually cancelled */
    CANCELLED
}
