package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;

/**
 * Use case for adding items to a cart.
 */
public interface AddItemUseCase {
    AddItemResult addItem(AddItemCommand command);
}
