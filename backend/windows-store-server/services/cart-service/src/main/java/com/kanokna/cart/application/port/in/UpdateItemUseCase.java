package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.UpdateItemCommand;

/**
 * Use case for updating cart items.
 */
public interface UpdateItemUseCase {
    CartDto updateItem(UpdateItemCommand command);
}
