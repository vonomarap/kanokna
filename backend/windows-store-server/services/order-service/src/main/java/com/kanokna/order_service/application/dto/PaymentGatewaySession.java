package com.kanokna.order_service.application.dto;

public record PaymentGatewaySession(
    String redirectUrl,
    String sessionId
) { }
