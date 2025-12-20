package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

import java.util.Objects;

public record PaymentCallbackCommand(
    Id orderId,
    Id paymentId,
    String gatewayStatus,
    String externalRef,
    String messageId,
    String idempotencyKey
) {
    public PaymentCallbackCommand {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(paymentId, "paymentId");
        Objects.requireNonNull(gatewayStatus, "gatewayStatus");
        Objects.requireNonNull(messageId, "messageId");
    }
}
