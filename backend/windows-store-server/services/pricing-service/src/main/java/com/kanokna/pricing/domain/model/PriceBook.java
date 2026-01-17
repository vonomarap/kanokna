package com.kanokna.pricing.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root for price definitions.
 * Contains base prices and option premiums for product templates.
 * Supports versioning per DEC-ARCH-ENTITY-VERSIONING.
 */
public class PriceBook {
    private final PriceBookId id;
    private final String productTemplateId;
    private final String currency;
    private PriceBookStatus status;
    private final BasePriceEntry basePriceEntry;
    private final List<OptionPremium> optionPremiums;
    private long version;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;

    private PriceBook(PriceBookId id, String productTemplateId, String currency,
                     BasePriceEntry basePriceEntry, String createdBy,
                     PriceBookStatus status, long version,
                     Instant createdAt, Instant updatedAt,
                     List<OptionPremium> optionPremiums) {
        this.id = Objects.requireNonNull(id);
        this.productTemplateId = Objects.requireNonNull(productTemplateId);
        this.currency = Objects.requireNonNull(currency);
        this.basePriceEntry = Objects.requireNonNull(basePriceEntry);
        this.createdBy = createdBy;
        this.status = Objects.requireNonNull(status);
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.optionPremiums = new ArrayList<>(optionPremiums != null ? optionPremiums : List.of());
    }

    public static PriceBook create(PriceBookId id, String productTemplateId, String currency,
                                   BasePriceEntry basePriceEntry, String createdBy) {
        Instant now = Instant.now();
        return new PriceBook(id, productTemplateId, currency, basePriceEntry, createdBy,
            PriceBookStatus.DRAFT, 0, now, now, new ArrayList<>());
    }

    public static PriceBook restore(PriceBookId id, String productTemplateId, String currency,
                                    BasePriceEntry basePriceEntry, String createdBy,
                                    PriceBookStatus status, long version,
                                    Instant createdAt, Instant updatedAt,
                                    List<OptionPremium> optionPremiums) {
        return new PriceBook(id, productTemplateId, currency, basePriceEntry, createdBy,
            status, version, createdAt, updatedAt, optionPremiums);
    }

    public void addOptionPremium(OptionPremium premium) {
        if (status != PriceBookStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify non-draft price book");
        }
        optionPremiums.add(premium);
        this.updatedAt = Instant.now();
    }

    public void publish() {
        if (status != PriceBookStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT price books can be published");
        }
        this.status = PriceBookStatus.ACTIVE;
        this.version++;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        if (status != PriceBookStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE price books can be archived");
        }
        this.status = PriceBookStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public Optional<OptionPremium> findPremiumForOption(String optionId) {
        return optionPremiums.stream()
            .filter(premium -> premium.getOptionId().equals(optionId))
            .findFirst();
    }

    public boolean isActive() {
        return status == PriceBookStatus.ACTIVE;
    }

    // Getters
    public PriceBookId getId() {
        return id;
    }

    public String getProductTemplateId() {
        return productTemplateId;
    }

    public String getCurrency() {
        return currency;
    }

    public PriceBookStatus getStatus() {
        return status;
    }

    public BasePriceEntry getBasePriceEntry() {
        return basePriceEntry;
    }

    public List<OptionPremium> getOptionPremiums() {
        return Collections.unmodifiableList(optionPremiums);
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceBook priceBook = (PriceBook) o;
        return id.equals(priceBook.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
