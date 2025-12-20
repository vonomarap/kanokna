package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

public record PaymentResult(
    Id orderId,
    Id paymentId,
    String status,
    PaymentGatewaySession session
) { }
