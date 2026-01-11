package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.PromoCode;
import java.util.Optional;

/**
 * Outbound port for promo code persistence.
 */
public interface PromoCodeRepository {
    Optional<PromoCode> findByCode(String code);

    PromoCode save(PromoCode promoCode);
}
