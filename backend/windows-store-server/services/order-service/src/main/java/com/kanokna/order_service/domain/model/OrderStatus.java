package com.kanokna.order_service.domain.model;

public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    IN_PRODUCTION,
    READY_TO_SHIP,
    SHIPPED,
    DELIVERED,
    INSTALLED,
    CANCELLED
}
