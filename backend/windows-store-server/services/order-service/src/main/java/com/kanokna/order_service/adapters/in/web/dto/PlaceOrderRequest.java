package com.kanokna.order_service.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
    @NotBlank String orderId,
    @NotBlank String cartId,
    String customerId,
    ShippingDto shipping,
    InstallationDto installation,
    String idempotencyKey
) {
    public record ShippingDto(
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country,
        @NotBlank String method
    ) {}

    public record InstallationDto(
        @NotNull java.time.Instant scheduledAt,
        String installerId,
        String notes
    ) {}
}
