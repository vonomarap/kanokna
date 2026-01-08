package com.kanokna.account.domain.model;

/**
 * Value object for preferred currency (ISO-4217).
 */
public record CurrencyPreference(String currencyCode) {
    public CurrencyPreference {
        if (currencyCode != null) {
            currencyCode = currencyCode.strip();
        }
    }
}