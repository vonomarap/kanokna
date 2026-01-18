package com.kanokna.search.adapters.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for search-service using immutable record pattern.
 */
@Validated
@ConfigurationProperties(prefix = "kanokna.search")
public record SearchProperties(
    @Valid @NotNull Index index,
    @Valid @NotNull Reindex reindex
) {
    /**
     * Compact constructor providing null-safe defaults.
     */
    public SearchProperties {
        index = index != null ? index
            : new Index("product_templates", "product_templates", "product_templates_v");
        reindex = reindex != null ? reindex
            : new Reindex(200, "search-service:reindex-lock");
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
}
