package com.kanokna.order_service.domain.model;

import java.util.Objects;

public record ShippingInfo(
    String addressLine1,
    String addressLine2,
    String city,
    String postalCode,
    String country,
    String method
) {
    public ShippingInfo {
        Objects.requireNonNull(addressLine1, "addressLine1");
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(postalCode, "postalCode");
        Objects.requireNonNull(country, "country");
        Objects.requireNonNull(method, "method");
    }
}
