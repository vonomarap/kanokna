package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.RemovePromoCodeCommand;

/**
 * Use case for removing promo codes.
 */
public interface RemovePromoCodeUseCase {
    CartDto removePromoCode(RemovePromoCodeCommand command);
}
