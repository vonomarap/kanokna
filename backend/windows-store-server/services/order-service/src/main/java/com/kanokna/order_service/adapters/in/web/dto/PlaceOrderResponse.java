package com.kanokna.order_service.adapters.in.web.dto;

public record PlaceOrderResponse(
    String orderId,
    String status
) { }
