package com.kanokna.cart.application.port.out;

import com.kanokna.cart.domain.model.Cart;
import java.util.Optional;

/**
 * Outbound port for cart persistence.
 */
public interface CartRepository {
    Optional<Cart> findByCustomerId(String customerId);

    Optional<Cart> findBySessionId(String sessionId);

    Cart save(Cart cart);
}
