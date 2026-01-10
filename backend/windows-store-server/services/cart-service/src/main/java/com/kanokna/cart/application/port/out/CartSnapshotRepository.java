package com.kanokna.cart.application.port.out;

import com.kanokna.cart.domain.model.CartSnapshot;

/**
 * Outbound port for cart snapshot persistence.
 */
public interface CartSnapshotRepository {
    CartSnapshot save(CartSnapshot snapshot);
}
