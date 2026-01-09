package com.kanokna.search.application.dto;

import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.shared.money.Money;

import java.time.Instant;
import java.util.List;

/**
 * Catalog product event payload for indexing.
 */
public class CatalogProductEvent {
    private final String eventId;
    private final String eventType;
    private final String productId;
    private final String name;
    private final String description;
    private final String family;
    private final String profileSystem;
    private final List<String> openingTypes;
    private final List<String> materials;
    private final List<String> colors;
    private final Money basePrice;
    private final Money maxPrice;
    private final ProductStatus status;
    private final String thumbnailUrl;
    private final int popularity;
    private final int optionGroupCount;
    private final Instant publishedAt;
    private final Instant updatedAt;

    public CatalogProductEvent(
        String eventId,
        String eventType,
        String productId,
        String name,
        String description,
        String family,
        String profileSystem,
        List<String> openingTypes,
        List<String> materials,
        List<String> colors,
        Money basePrice,
        Money maxPrice,
        ProductStatus status,
        String thumbnailUrl,
        int popularity,
        int optionGroupCount,
        Instant publishedAt,
        Instant updatedAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.family = family;
        this.profileSystem = profileSystem;
        this.openingTypes = openingTypes == null ? List.of() : List.copyOf(openingTypes);
        this.materials = materials == null ? List.of() : List.copyOf(materials);
        this.colors = colors == null ? List.of() : List.copyOf(colors);
        this.basePrice = basePrice;
        this.maxPrice = maxPrice;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
        this.popularity = popularity;
        this.optionGroupCount = optionGroupCount;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFamily() {
        return family;
    }

    public String getProfileSystem() {
        return profileSystem;
    }

    public List<String> getOpeningTypes() {
        return openingTypes;
    }

    public List<String> getMaterials() {
        return materials;
    }

    public List<String> getColors() {
        return colors;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public Money getMaxPrice() {
        return maxPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getPopularity() {
        return popularity;
    }

    public int getOptionGroupCount() {
        return optionGroupCount;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
