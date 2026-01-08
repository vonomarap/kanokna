package com.kanokna.pricing_service.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root for promotional campaigns.
 * Campaigns provide discount rules that apply to product configurations.
 */
public class Campaign {
    private final CampaignId id;
    private final String name;
    private final String description;
    private final CampaignRule rule;
    private final Set<String> applicableProducts;
    private final Instant startDate;
    private final Instant endDate;
    private CampaignStatus status;
    private final int priority;
    private final Instant createdAt;
    private final String createdBy;

    private Campaign(CampaignId id, String name, String description, CampaignRule rule,
                    Set<String> applicableProducts, Instant startDate, Instant endDate,
                    int priority, String createdBy, CampaignStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.rule = Objects.requireNonNull(rule);
        this.applicableProducts = new HashSet<>(applicableProducts != null ? applicableProducts : Set.of());
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        this.priority = priority;
        this.createdBy = createdBy;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public static Campaign create(CampaignId id, String name, String description, CampaignRule rule,
                                  Set<String> applicableProducts, Instant startDate, Instant endDate,
                                  int priority, String createdBy) {
        CampaignStatus status = determineStatus(startDate, endDate, Instant.now());
        return new Campaign(id, name, description, rule, applicableProducts, startDate, endDate,
            priority, createdBy, status, Instant.now());
    }

    public static Campaign restore(CampaignId id, String name, String description, CampaignRule rule,
                                   Set<String> applicableProducts, Instant startDate, Instant endDate,
                                   CampaignStatus status, int priority, Instant createdAt, String createdBy) {
        return new Campaign(id, name, description, rule, applicableProducts, startDate, endDate,
            priority, createdBy, status, createdAt);
    }

    private static CampaignStatus determineStatus(Instant startDate, Instant endDate, Instant now) {
        if (now.isBefore(startDate)) {
            return CampaignStatus.SCHEDULED;
        } else if (now.isAfter(endDate)) {
            return CampaignStatus.EXPIRED;
        }
        return CampaignStatus.ACTIVE;
    }

    public void cancel() {
        this.status = CampaignStatus.CANCELLED;
    }

    public void updateStatus() {
        if (status != CampaignStatus.CANCELLED) {
            this.status = determineStatus(startDate, endDate, Instant.now());
        }
    }

    public boolean isApplicableTo(String productTemplateId) {
        if (applicableProducts.isEmpty()) {
            return true; // Applies to all products
        }
        return applicableProducts.contains(productTemplateId);
    }

    public boolean isActive() {
        return status == CampaignStatus.ACTIVE;
    }

    public Money applyDiscount(Money subtotal) {
        if (!isActive()) {
            return Money.zero(subtotal.getCurrency());
        }
        return rule.calculateDiscount(subtotal);
    }

    // Getters
    public CampaignId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CampaignRule getRule() {
        return rule;
    }

    public Set<String> getApplicableProducts() {
        return Set.copyOf(applicableProducts);
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Campaign campaign = (Campaign) o;
        return id.equals(campaign.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
