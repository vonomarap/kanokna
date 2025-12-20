package com.kanokna.order_service.adapters.in.web.dto;

import java.util.List;

public record OrderSummaryResponse(
    String orderId,
    String status,
    String currency,
    List<OrderItemDto> items
) {
    public record OrderItemDto(String configurationId, int quantity) {}
}
