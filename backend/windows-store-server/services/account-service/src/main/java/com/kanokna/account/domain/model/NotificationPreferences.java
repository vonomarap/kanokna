package com.kanokna.account.domain.model;

/**
 * Notification preferences for a user profile.
 */
public record NotificationPreferences(
    boolean email,
    boolean sms,
    boolean push
) {
    public static NotificationPreferences of(boolean email, boolean sms, boolean push) {
        return new NotificationPreferences(email, sms, push);
    }
}