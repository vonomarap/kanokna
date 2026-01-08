package com.kanokna.account.domain.model;

/**
 * Value object for preferred locale (BCP-47 language tag).
 */
public record LocalePreference(String languageTag) {
    public LocalePreference {
        if (languageTag != null) {
            languageTag = languageTag.strip();
        }
    }
}