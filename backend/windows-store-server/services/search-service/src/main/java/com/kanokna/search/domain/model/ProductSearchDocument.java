package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Money;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Denormalized product document optimized for search.
 */
public class ProductSearchDocument {
    private final String id;
    private final LocalizedString name;
    private final LocalizedString description;
    private final String family;
    private final String profileSystem;
    private final List<String> openingTypes;
    private final List<String> materials;
    private final List<String> colors;
    private final Money minPrice;
    private final Money maxPrice;
    private final String currency;
    private final int popularity;
    private final ProductStatus status;
    private final Instant publishedAt;
    private final String thumbnailUrl;
    private final int optionCount;
    private final List<String> suggestInputs;
    private final Float score;
    private final Map<String, String> highlights;

    private ProductSearchDocument(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = builder.name;
        this.description = builder.description;
        this.family = builder.family;
        this.profileSystem = builder.profileSystem;
        this.openingTypes = builder.openingTypes == null ? List.of() : List.copyOf(builder.openingTypes);
        this.materials = builder.materials == null ? List.of() : List.copyOf(builder.materials);
        this.colors = builder.colors == null ? List.of() : List.copyOf(builder.colors);
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.currency = builder.currency;
        this.popularity = builder.popularity;
        this.status = builder.status == null ? ProductStatus.UNSPECIFIED : builder.status;
        this.publishedAt = builder.publishedAt;
        this.thumbnailUrl = builder.thumbnailUrl;
        this.optionCount = builder.optionCount;
        this.suggestInputs = builder.suggestInputs == null ? List.of() : List.copyOf(builder.suggestInputs);
        this.score = builder.score;
        this.highlights = builder.highlights == null ? Map.of() : Map.copyOf(builder.highlights);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getId() {
        return id;
    }

    public LocalizedString getName() {
        return name;
    }

    public LocalizedString getDescription() {
        return description;
    }

    public String getFamily() {
        return family;
    }

    public String getProfileSystem() {
        return profileSystem;
    }

    public List<String> getOpeningTypes() {
        return Collections.unmodifiableList(openingTypes);
    }

    public List<String> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    public List<String> getColors() {
        return Collections.unmodifiableList(colors);
    }

    public Money getMinPrice() {
        return minPrice;
    }

    public Money getMaxPrice() {
        return maxPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public int getPopularity() {
        return popularity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getOptionCount() {
        return optionCount;
    }

    public List<String> getSuggestInputs() {
        return Collections.unmodifiableList(suggestInputs);
    }

    public Float getScore() {
        return score;
    }

    public Map<String, String> getHighlights() {
        return Collections.unmodifiableMap(highlights);
    }

    public static class Builder {
        private final String id;
        private LocalizedString name;
        private LocalizedString description;
        private String family;
        private String profileSystem;
        private List<String> openingTypes;
        private List<String> materials;
        private List<String> colors;
        private Money minPrice;
        private Money maxPrice;
        private String currency;
        private int popularity;
        private ProductStatus status;
        private Instant publishedAt;
        private String thumbnailUrl;
        private int optionCount;
        private List<String> suggestInputs;
        private Float score;
        private Map<String, String> highlights;

        public Builder(String id) {
            this.id = id;
        }

        public Builder name(LocalizedString name) {
            this.name = name;
            return this;
        }

        public Builder description(LocalizedString description) {
            this.description = description;
            return this;
        }

        public Builder family(String family) {
            this.family = family;
            return this;
        }

        public Builder profileSystem(String profileSystem) {
            this.profileSystem = profileSystem;
            return this;
        }

        public Builder openingTypes(List<String> openingTypes) {
            this.openingTypes = openingTypes;
            return this;
        }

        public Builder materials(List<String> materials) {
            this.materials = materials;
            return this;
        }

        public Builder colors(List<String> colors) {
            this.colors = colors;
            return this;
        }

        public Builder minPrice(Money minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(Money maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder popularity(int popularity) {
            this.popularity = popularity;
            return this;
        }

        public Builder status(ProductStatus status) {
            this.status = status;
            return this;
        }

        public Builder publishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public Builder optionCount(int optionCount) {
            this.optionCount = optionCount;
            return this;
        }

        public Builder suggestInputs(List<String> suggestInputs) {
            this.suggestInputs = suggestInputs;
            return this;
        }

        public Builder score(Float score) {
            this.score = score;
            return this;
        }

        public Builder highlights(Map<String, String> highlights) {
            this.highlights = highlights;
            return this;
        }

        public ProductSearchDocument build() {
            return new ProductSearchDocument(this);
        }
    }
}
