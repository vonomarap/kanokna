package com.kanokna.cart.application.port.out;

import java.util.Optional;

/**
 * Outbound port for anonymous session cart storage.
 */
public interface SessionCartStore {
    Optional<String> findCartId(String sessionId);

    void storeCartId(String sessionId, String cartId);

    void removeCartId(String sessionId);
}
