package com.kanokna.account.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for account-service.
 */
@ConfigurationProperties(prefix = "account")
public class AccountProperties {
    private boolean autoCreateProfile = true;
    private int maxAddressesPerUser = 10;
    private SavedConfigurations savedConfigurations = new SavedConfigurations();
    private Defaults defaults = new Defaults();

    public boolean isAutoCreateProfile() {
        return autoCreateProfile;
    }

    public void setAutoCreateProfile(boolean autoCreateProfile) {
        this.autoCreateProfile = autoCreateProfile;
    }

    public int getMaxAddressesPerUser() {
        return maxAddressesPerUser;
    }

    public void setMaxAddressesPerUser(int maxAddressesPerUser) {
        this.maxAddressesPerUser = maxAddressesPerUser;
    }

    public SavedConfigurations getSavedConfigurations() {
        return savedConfigurations;
    }

    public void setSavedConfigurations(SavedConfigurations savedConfigurations) {
        this.savedConfigurations = savedConfigurations;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public static class SavedConfigurations {
        private int maxPerUser = 50;
        private String defaultNamePattern = "Config {timestamp}";

        public int getMaxPerUser() {
            return maxPerUser;
        }

        public void setMaxPerUser(int maxPerUser) {
            this.maxPerUser = maxPerUser;
        }

        public String getDefaultNamePattern() {
            return defaultNamePattern;
        }

        public void setDefaultNamePattern(String defaultNamePattern) {
            this.defaultNamePattern = defaultNamePattern;
        }
    }

    public static class Defaults {
        private String language = "ru";
        private String currency = "RUB";
        private boolean notificationEmail = true;
        private boolean notificationSms = false;
        private boolean notificationPush = true;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public boolean isNotificationEmail() {
            return notificationEmail;
        }

        public void setNotificationEmail(boolean notificationEmail) {
            this.notificationEmail = notificationEmail;
        }

        public boolean isNotificationSms() {
            return notificationSms;
        }

        public void setNotificationSms(boolean notificationSms) {
            this.notificationSms = notificationSms;
        }

        public boolean isNotificationPush() {
            return notificationPush;
        }

        public void setNotificationPush(boolean notificationPush) {
            this.notificationPush = notificationPush;
        }
    }
}