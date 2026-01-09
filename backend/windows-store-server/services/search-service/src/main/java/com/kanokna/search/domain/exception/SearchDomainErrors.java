package com.kanokna.search.domain.exception;

import com.kanokna.shared.core.DomainException;

/**
 * Factory methods for search-service domain errors.
 */
public final class SearchDomainErrors {
    private SearchDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainException elasticsearchUnavailable(String message) {
        return new DomainException("ERR-SEARCH-ES-UNAVAILABLE", message);
    }

    public static DomainException indexNotFound(String indexName) {
        return new DomainException("ERR-SEARCH-INDEX-NOT-FOUND",
            "Index not found: " + indexName);
    }

    public static DomainException queryTimeout(String message) {
        return new DomainException("ERR-SEARCH-QUERY-TIMEOUT", message);
    }

    public static DomainException invalidFacetField(String field) {
        return new DomainException("ERR-SEARCH-INVALID-FACET",
            "Invalid facet field: " + field);
    }

    public static DomainException autocompletePrefixTooShort(String prefix) {
        return new DomainException("ERR-AUTO-PREFIX-TOO-SHORT",
            "Prefix too short: " + prefix);
    }

    public static DomainException autocompleteElasticsearchUnavailable(String message) {
        return new DomainException("ERR-AUTO-ES-UNAVAILABLE", message);
    }

    public static DomainException indexElasticsearchUnavailable(String message) {
        return new DomainException("ERR-INDEX-ES-UNAVAILABLE", message);
    }

    public static DomainException indexMappingError(String message) {
        return new DomainException("ERR-INDEX-MAPPING-ERROR", message);
    }

    public static DomainException invalidIndexEvent(String message) {
        return new DomainException("ERR-INDEX-INVALID-EVENT", message);
    }

    public static DomainException deleteElasticsearchUnavailable(String message) {
        return new DomainException("ERR-DELETE-ES-UNAVAILABLE", message);
    }

    public static DomainException facetElasticsearchUnavailable(String message) {
        return new DomainException("ERR-FACET-ES-UNAVAILABLE", message);
    }

    public static DomainException invalidFacetValuesField(String field) {
        return new DomainException("ERR-FACET-INVALID-FIELD",
            "Invalid facet field: " + field);
    }

    public static DomainException getElasticsearchUnavailable(String message) {
        return new DomainException("ERR-GET-ES-UNAVAILABLE", message);
    }

    public static DomainException productNotFound(String productId) {
        return new DomainException("ERR-GET-PRODUCT-NOT-FOUND",
            "Product not found: " + productId);
    }

    public static DomainException reindexElasticsearchUnavailable(String message) {
        return new DomainException("ERR-REINDEX-ES-UNAVAILABLE", message);
    }

    public static DomainException reindexCatalogUnavailable(String message) {
        return new DomainException("ERR-REINDEX-CATALOG-UNAVAILABLE", message);
    }

    public static DomainException reindexInProgress() {
        return new DomainException("ERR-REINDEX-IN-PROGRESS",
            "Reindex already in progress");
    }

    public static DomainException reindexAliasSwapFailed(String message) {
        return new DomainException("ERR-REINDEX-ALIAS-SWAP-FAILED", message);
    }

    public static DomainException reindexLockUnavailable(String message) {
        return new DomainException("ERR-REINDEX-LOCK-UNAVAILABLE", message);
    }
}
