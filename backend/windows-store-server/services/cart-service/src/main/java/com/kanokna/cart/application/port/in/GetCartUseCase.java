package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.GetCartQuery;

/**
 * Use case for retrieving carts.
 */
public interface GetCartUseCase {
    CartDto getCart(GetCartQuery query);
}
