package com.kanokna.pricing_service.domain.exception;

/**
 * Domain-level exception for pricing invariants or data issues.
 * Adapters are responsible for mapping this to transport-level errors.
 */
public class PricingDomainException extends RuntimeException {
    public PricingDomainException(String message) {
        super(message);
    }

    public PricingDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
