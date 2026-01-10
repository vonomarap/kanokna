package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.RemoveItemCommand;

/**
 * Use case for removing cart items.
 */
public interface RemoveItemUseCase {
    CartDto removeItem(RemoveItemCommand command);
}
