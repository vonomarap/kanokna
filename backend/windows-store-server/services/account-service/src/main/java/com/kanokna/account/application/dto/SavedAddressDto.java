package com.kanokna.account.application.dto;

import java.time.Instant;

/**
 * DTO representing a saved address.
 */
public record SavedAddressDto(
    String addressId,
    AddressDto address,
    String label,
    boolean isDefault,
    Instant createdAt,
    Instant updatedAt
) {
}