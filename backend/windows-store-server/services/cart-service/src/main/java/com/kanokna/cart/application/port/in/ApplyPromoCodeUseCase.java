package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;

/**
 * Use case for applying promo codes.
 */
public interface ApplyPromoCodeUseCase {
    ApplyPromoCodeResult applyPromoCode(ApplyPromoCodeCommand command);
}
