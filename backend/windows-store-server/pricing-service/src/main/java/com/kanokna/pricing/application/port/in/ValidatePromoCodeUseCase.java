package com.kanokna.pricing.application.port.in;

import com.kanokna.pricing.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing.application.dto.ValidatePromoCodeCommand;

/**
 * Inbound port for validating promotional codes.
 */
public interface ValidatePromoCodeUseCase {
    PromoCodeValidationResponse validatePromoCode(ValidatePromoCodeCommand command);
}
