package com.kanokna.account.domain.exception;

import com.kanokna.shared.core.DomainException;

/**
 * MODULE_CONTRACT id="MC-account-domain-errors" LAYER="domain.exception"
 * INTENT="Factory methods for account domain error codes ensuring consistent
 * error handling" LINKS="MC-account-service-domain;Technology.xml#ERR-HANDLING"
 *
 * Factory methods for account domain error codes.
 */
public final class AccountDomainErrors {

    private AccountDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainException unauthorized(String message) {
        return new DomainException("ERR-ACCT-UNAUTHORIZED", message);
    }

    public static DomainException keycloakUnavailable(String message, Throwable cause) {
        return new DomainException("ERR-ACCT-KEYCLOAK-UNAVAILABLE", message, cause);
    }

    public static DomainException profileNotFound(String userId) {
        return new DomainException("ERR-ACCT-PROFILE-NOT-FOUND",
                "Profile does not exist for userId=" + userId);
    }

    public static DomainException invalidPhone(String value) {
        return new DomainException("ERR-ACCT-INVALID-PHONE",
                "Phone number format invalid: " + value);
    }

    public static DomainException invalidLanguage(String value) {
        return new DomainException("ERR-ACCT-INVALID-LANGUAGE",
                "Language code not recognized: " + value);
    }

    public static DomainException invalidCurrency(String value) {
        return new DomainException("ERR-ACCT-INVALID-CURRENCY",
                "Currency code not recognized: " + value);
    }

    public static DomainException concurrentModification(String userId) {
        return new DomainException("ERR-ACCT-CONCURRENT-MODIFICATION",
                "Optimistic lock failure for userId=" + userId);
    }

    public static DomainException invalidConfigName(String value) {
        return new DomainException("ERR-ACCT-INVALID-CONFIG-NAME",
                "Configuration name is invalid: " + value);
    }

    public static DomainException invalidConfigSnapshot(String reason) {
        return new DomainException("ERR-ACCT-INVALID-CONFIG-SNAPSHOT", reason);
    }

    public static DomainException addressNotFound(String addressId) {
        return new DomainException("ERR-ACCT-ADDRESS-NOT-FOUND",
                "Address does not exist for addressId=" + addressId);
    }

    public static DomainException addressDuplicate(String city) {
        return new DomainException("ERR-ACCT-ADDRESS-DUPLICATE",
                "Address already exists for city=" + city);
    }

    public static DomainException invalidAddress(String message) {
        return new DomainException("ERR-ACCT-INVALID-ADDRESS", message);
    }

    public static DomainException labelTooLong(String label) {
        return new DomainException("ERR-ACCT-LABEL-TOO-LONG",
                "Label exceeds 50 characters: " + label);
    }
}
