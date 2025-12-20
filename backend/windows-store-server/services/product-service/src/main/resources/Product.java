package com.kanokna.product_service.domain.model;

import com.kanokna.shared.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Aggregate root for catalog products.
 * Mutable by intention (aggregate root), with defensive copies on read.
 */
public final class Product {
  private final Id id;
  private LocalizedString name;
  private ProductStatus status;
  private final List<ProductVariant> variants;
  private final List<Certification> certifications;

  private Product(Id id,
                  LocalizedString name,
                  ProductStatus status,
                  List<ProductVariant> variants,
                  List<Certification> certifications) {
    this.id = requireNonNull(id, "id");
    this.name = requireNonNull(name, "name");
    this.status = requireNonNull(status, "status");
    this.variants = new ArrayList<>(requireNonNull(variants, "variants"));
    this.certifications = new ArrayList<>(requireNonNull(certifications, "certifications"));
    if (this.variants.stream().map(ProductVariant::sku).collect(Collectors.toSet()).size() != this.variants.size()) {
      throw new IllegalArgumentException("duplicate variant SKU");
    }
  }

  public static Product of(Id id,
                           LocalizedString name,
                           ProductStatus status,
                           List<ProductVariant> variants,
                           List<Certification> certifications) {
    return new Product(id, name, status, variants == null ? List.of() : variants,
      certifications == null ? List.of() : certifications);
  }

  public void activate() {
    if (status == ProductStatus.RETIRED) {
      throw new IllegalStateException("Cannot activate retired product");
    }
    this.status = ProductStatus.ACTIVE;
  }

  public void retire() {
    this.status = ProductStatus.RETIRED;
  }

  public boolean isActive() {
    return status == ProductStatus.ACTIVE;
  }

  public Optional<ProductVariant> findVariantBySku(String sku) {
    requireNonNull(sku, "sku");
    return variants.stream().filter(v -> v.sku().value().equals(sku)).findFirst();
  }

  public Id id() { return id; }
  public LocalizedString name() { return name; }
  public ProductStatus status() { return status; }
  public List<ProductVariant> variants() { return List.copyOf(variants); }
  public List<Certification> certifications() { return List.copyOf(certifications); }

  public void rename(LocalizedString newName) { this.name = requireNonNull(newName, "newName"); }

  @Override public String toString() {
    return "Product{id=%s, status=%s, variants=%d}".formatted(id, status, variants.size());
  }
}

