package com.kanokna.account.application.dto;

/**
 * DTO for notification preferences.
 */
public record NotificationPreferencesDto(
    boolean email,
    boolean sms,
    boolean push
) {
}