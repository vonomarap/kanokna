package com.kanokna.account.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for account-service using immutable record pattern.
 */
@Validated
@ConfigurationProperties(prefix = "kanokna.account")
public record AccountProperties(
    boolean autoCreateProfile,
    @Positive int maxAddressesPerUser,
    @Valid @NotNull SavedConfigurations savedConfigurations,
    @Valid @NotNull Defaults defaults
) {
    /**
     * Compact constructor providing null-safe defaults.
     */
    public AccountProperties {
        maxAddressesPerUser = maxAddressesPerUser > 0 ? maxAddressesPerUser : 10;
        savedConfigurations = savedConfigurations != null ? savedConfigurations
            : new SavedConfigurations(50, "Config {timestamp}");
        defaults = defaults != null ? defaults
            : new Defaults("ru", "RUB", true, false, true);
    }

    /**
     * Saved configuration limits.
     */
    public record SavedConfigurations(
        /** Maximum saved configurations per user. Default: 50 */
        @Positive int maxPerUser,
        /** Default name pattern for saved configs. Default: "Config {timestamp}" */
        @NotBlank String defaultNamePattern
    ) {}

    /**
     * Default values for new user preferences.
     */
    public record Defaults(
        /** Default language code. Default: "ru" */
        @NotBlank String language,
        /** Default currency code. Default: "RUB" */
        @NotBlank String currency,
        /** Email notifications enabled by default. Default: true */
        boolean notificationEmail,
        /** SMS notifications enabled by default. Default: false */
        boolean notificationSms,
        /** Push notifications enabled by default. Default: true */
        boolean notificationPush
    ) {}
}
