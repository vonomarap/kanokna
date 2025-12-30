package com.kanokna.shared.money;

/**
 * Supported currencies for the windows and doors e-commerce platform.
 * <p>
 * This enum defines the currencies supported by the platform with their
 * display names, symbols, and default decimal scales.
 *
 * @see Money
 * @see <a href="RequirementsAnalysis.xml#NFR-I18N-MULTI-CURRENCY">NFR-I18N-MULTI-CURRENCY</a>
 */
public enum Currency {

    /**
     * Russian Ruble - Primary currency for Russian market.
     */
    RUB("Russian Ruble", "\u20BD", 2),

    /**
     * Euro - For European markets.
     */
    EUR("Euro", "\u20AC", 2),

    /**
     * US Dollar - For international transactions.
     */
    USD("US Dollar", "$", 2);

    private final String displayName;
    private final String symbol;
    private final int defaultScale;

    Currency(String displayName, String symbol, int defaultScale) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.defaultScale = defaultScale;
    }

    /**
     * Returns the human-readable name of this currency.
     *
     * @return the display name (e.g., "Russian Ruble")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the symbol (e.g., "₽", "€", "$")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the default number of decimal places for this currency.
     *
     * @return the default scale (typically 2 for most currencies)
     */
    public int getDefaultScale() {
        return defaultScale;
    }
}
