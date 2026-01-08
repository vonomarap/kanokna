package com.kanokna.account.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO representing a user profile with addresses.
 */
public record UserProfileDto(
    String userId,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String preferredLanguage,
    String preferredCurrency,
    NotificationPreferencesDto notificationPreferences,
    List<SavedAddressDto> addresses,
    Instant createdAt,
    Instant updatedAt
) {
}