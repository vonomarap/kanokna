package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Objects;

public record CartSnapshot(
    Id cartId,
    Id customerId,
    List<CartItemSnapshot> items
) {
    public CartSnapshot {
        Objects.requireNonNull(cartId, "cartId");
        Objects.requireNonNull(items, "items");
    }

    public record CartItemSnapshot(Id configurationId, int quantity) { }
}
