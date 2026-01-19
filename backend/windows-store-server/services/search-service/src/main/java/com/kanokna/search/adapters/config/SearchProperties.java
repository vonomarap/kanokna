package com.kanokna.search.adapters.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.money.Currency;

/**
 * MODULE_CONTRACT id="MC-search-properties"
 * LAYER="adapters.config"
 * INTENT="Externalize all search-service configuration; eliminate magic numbers"
 * LINKS="Technology.xml#DEC-CONFIG-PROPERTIES-PATTERN;RequirementsAnalysis.xml#UC-CATALOG-BROWSE"
 *
 * Configuration properties for search-service using immutable record pattern.
 */
@Validated
@ConfigurationProperties(prefix = "kanokna.search")
public record SearchProperties(
    @Valid @NotNull Index index,
    @Valid @NotNull Reindex reindex,
    @Valid @NotNull Autocomplete autocomplete,
    @Valid @NotNull Facets facets,
    @Valid @NotNull Defaults defaults
) {
    /**
     * Compact constructor providing null-safe defaults.
     */
    public SearchProperties {
        index = index != null ? index
            : new Index("product_templates", "product_templates", "product_templates_v");
        reindex = reindex != null ? reindex
            : new Reindex(200, "search-service:reindex-lock");
        autocomplete = autocomplete != null ? autocomplete
            : new Autocomplete(2, 10, 20);
        facets = facets != null ? facets
            : new Facets(List.of("family", "profileSystem", "materials", "colors", "openingTypes"));
        defaults = defaults != null ? defaults
            : new Defaults(Language.RU, Currency.RUB);
    }

    /**
     * Elasticsearch index configuration.
     */
    public record Index(
        /** Index name. Default: "product_templates" */
        @NotBlank String name,
        /** Index alias. Default: "product_templates" */
        @NotBlank String alias,
        /** Version prefix for index naming. Default: "product_templates_v" */
        @NotBlank String versionPrefix
    ) {}

    /**
     * Reindexing configuration.
     */
    public record Reindex(
        /** Batch size for reindexing. Default: 200 */
        @Positive int batchSize,
        /** Distributed lock name. Default: "search-service:reindex-lock" */
        @NotBlank String lockName
    ) {}

    /**
     * Autocomplete configuration.
     */
    public record Autocomplete(
        /** Minimum prefix length for autocomplete. Default: 2 */
        @Positive int minPrefixLength,
        /** Default limit for autocomplete. Default: 10 */
        @Positive int defaultLimit,
        /** Maximum limit for autocomplete. Default: 20 */
        @Positive int maxLimit
    ) {}

    /**
     * Facet configuration.
     */
    public record Facets(
        /** Valid facet field names for filters. */
        @NotNull List<String> validFields
    ) {}

    /**
     * Default values for search queries.
     */
    public record Defaults(
        /** Default language for requests. Default: RU */
        @NotNull Language defaultLanguage,
        /** Default currency for indexed documents. Default: RUB */
        @NotNull Currency defaultCurrency
    ) {}
}
