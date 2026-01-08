package com.kanokna.account.domain.model;

import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.shared.core.Address;
import com.kanokna.shared.core.Id;

import java.time.Instant;
import java.util.Objects;

/**
 * Saved address value object associated with a user.
 */
public final class SavedAddress {
    private final Id addressId;
    private final Address address;
    private final String label;
    private final boolean isDefault;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SavedAddress(Id addressId,
                         Address address,
                         String label,
                         boolean isDefault,
                         Instant createdAt,
                         Instant updatedAt) {
        this.addressId = Objects.requireNonNull(addressId, "addressId cannot be null");
        this.address = Objects.requireNonNull(address, "address cannot be null");
        this.label = label;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SavedAddress create(Id addressId,
                                      Address address,
                                      String label,
                                      boolean isDefault,
                                      Instant now) {
        String validatedLabel = validateLabel(label);
        return new SavedAddress(addressId, address, validatedLabel, isDefault, now, now);
    }

    public static SavedAddress rehydrate(Id addressId,
                                         Address address,
                                         String label,
                                         boolean isDefault,
                                         Instant createdAt,
                                         Instant updatedAt) {
        String validatedLabel = validateLabel(label);
        return new SavedAddress(addressId, address, validatedLabel, isDefault, createdAt, updatedAt);
    }

    public SavedAddress update(Address updatedAddress,
                               String updatedLabel,
                               Boolean setAsDefault,
                               Instant now) {
        Address nextAddress = updatedAddress != null ? updatedAddress : address;
        String nextLabel = updatedLabel != null ? validateLabel(updatedLabel) : label;
        boolean nextDefault = setAsDefault != null ? setAsDefault : isDefault;
        return new SavedAddress(addressId, nextAddress, nextLabel, nextDefault, createdAt, now);
    }

    public SavedAddress withDefault(boolean nextDefault, Instant now) {
        return new SavedAddress(addressId, address, label, nextDefault, createdAt, now);
    }

    public Id addressId() {
        return addressId;
    }

    public Address address() {
        return address;
    }

    public String label() {
        return label;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String validateLabel(String label) {
        if (label == null || label.isBlank()) {
            throw AccountDomainErrors.invalidAddress("Label must be non-blank");
        }
        String trimmed = label.strip();
        if (trimmed.length() > 50) {
            throw AccountDomainErrors.labelTooLong(trimmed);
        }
        return trimmed;
    }
}
