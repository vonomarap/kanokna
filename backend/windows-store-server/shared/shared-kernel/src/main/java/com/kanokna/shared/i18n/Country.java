package com.kanokna.shared.i18n;

import com.kanokna.shared.money.Currency;

/**
 * Supported countries for the windows and doors e-commerce platform.
 * <p>
 * Each country is associated with a default currency and language
 * for localization and pricing purposes.
 *
 * @see Currency
 * @see Language
 */
public enum Country {

    /**
     * Russian Federation - Primary market.
     */
    RU("Russia", "RUS", Currency.RUB, Language.RU),

    /**
     * Germany - European market.
     */
    DE("Germany", "DEU", Currency.EUR, Language.DE),

    /**
     * France - European market.
     */
    FR("France", "FRA", Currency.EUR, Language.FR),

    /**
     * United States - International market.
     */
    US("United States", "USA", Currency.USD, Language.EN),

    /**
     * United Kingdom - International market.
     */
    GB("United Kingdom", "GBR", Currency.USD, Language.EN);

    private final String displayName;
    private final String iso3Code;
    private final Currency defaultCurrency;
    private final Language defaultLanguage;

    Country(String displayName, String iso3Code, Currency defaultCurrency, Language defaultLanguage) {
        this.displayName = displayName;
        this.iso3Code = iso3Code;
        this.defaultCurrency = defaultCurrency;
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Returns the human-readable country name.
     *
     * @return the display name (e.g., "Russia")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the ISO 3166-1 alpha-3 code.
     *
     * @return the 3-letter country code (e.g., "RUS")
     */
    public String getIso3Code() {
        return iso3Code;
    }

    /**
     * Returns the ISO 3166-1 alpha-2 code (same as enum name).
     *
     * @return the 2-letter country code (e.g., "RU")
     */
    public String getIso2Code() {
        return name();
    }

    /**
     * Returns the default currency for this country.
     *
     * @return the default currency
     */
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    /**
     * Returns the default language for this country.
     *
     * @return the default language
     */
    public Language getDefaultLanguage() {
        return defaultLanguage;
    }
}
