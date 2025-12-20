package com.kanokna.order_service.domain.model;

import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.Objects;

public record OrderItem(
    Id configurationId,
    int quantity,
    Money unitPrice,
    Money totalPrice
) {
    public OrderItem {
        Objects.requireNonNull(configurationId, "configurationId");
        Objects.requireNonNull(unitPrice, "unitPrice");
        Objects.requireNonNull(totalPrice, "totalPrice");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (!unitPrice.getCurrency().equals(totalPrice.getCurrency())) {
            throw new IllegalArgumentException("currency mismatch between unit and total");
        }
    }
}
