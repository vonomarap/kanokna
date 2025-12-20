package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

public record OrderCreatedResponse(
    Id orderId,
    String status
) { }
