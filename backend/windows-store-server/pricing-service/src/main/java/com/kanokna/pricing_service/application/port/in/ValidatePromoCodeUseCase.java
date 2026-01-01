package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing_service.application.dto.ValidatePromoCodeCommand;

/**
 * Inbound port for validating promotional codes.
 */
public interface ValidatePromoCodeUseCase {
    PromoCodeValidationResponse validatePromoCode(ValidatePromoCodeCommand command);
}

