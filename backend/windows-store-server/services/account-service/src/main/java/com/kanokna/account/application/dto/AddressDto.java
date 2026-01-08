package com.kanokna.account.application.dto;

import com.kanokna.shared.core.Address;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for address payloads.
 */
public record AddressDto(
    @NotBlank String country,
    @NotBlank String city,
    @NotBlank String postalCode,
    @NotBlank String line1,
    String line2
) {
    public Address toValueObject() {
        return new Address(country, city, postalCode, line1, line2);
    }

    public static AddressDto fromValueObject(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDto(
            address.country(),
            address.city(),
            address.postalCode(),
            address.line1(),
            address.line2()
        );
    }
}