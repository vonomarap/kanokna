package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

import java.util.Objects;

public record InitiatePaymentCommand(
    Id orderId,
    String returnUrl,
    String cancelUrl
) {
    public InitiatePaymentCommand {
        Objects.requireNonNull(orderId, "orderId");
    }
}
