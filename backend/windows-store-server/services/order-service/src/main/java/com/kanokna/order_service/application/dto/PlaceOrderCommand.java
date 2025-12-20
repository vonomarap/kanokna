package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

import java.util.Objects;

public record PlaceOrderCommand(
    Id orderId,
    Id cartId,
    Id customerId,
    ShippingDto shipping,
    InstallationDto installation,
    String idempotencyKey
) {
    public PlaceOrderCommand {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(cartId, "cartId");
    }

    public record ShippingDto(String addressLine1, String addressLine2, String city, String postalCode, String country, String method) { }
    public record InstallationDto(java.time.Instant scheduledAt, String installerId, String notes) { }
}
