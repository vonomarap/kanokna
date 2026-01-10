package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.ClearCartCommand;

/**
 * Use case for clearing carts.
 */
public interface ClearCartUseCase {
    CartDto clearCart(ClearCartCommand command);
}
