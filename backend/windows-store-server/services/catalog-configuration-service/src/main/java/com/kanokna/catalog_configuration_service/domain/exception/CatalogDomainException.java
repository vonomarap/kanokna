package com.kanokna.catalog_configuration_service.domain.exception;

/**
 * Generic domain exception for catalog-configuration invariants and validation failures.
 * Domain layer should throw this instead of framework exceptions to keep adapters responsible
 * for transport-specific translations.
 */
public class CatalogDomainException extends RuntimeException {

    public CatalogDomainException(String message) {
        super(message);
    }

    public CatalogDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
