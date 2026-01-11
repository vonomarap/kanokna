package com.kanokna.search.application.dto;

import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.shared.money.Money;

import java.time.Instant;
import java.util.List;

/**
 * Catalog product event payload for indexing.
 */
public record CatalogProductEvent(String eventId, String eventType, String productId, String name, String description,
                                  String family, String profileSystem, List<String> openingTypes,
                                  List<String> materials, List<String> colors, Money basePrice, Money maxPrice,
                                  ProductStatus status, String thumbnailUrl, int popularity, int optionGroupCount,
                                  Instant publishedAt, Instant updatedAt) {
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
}
