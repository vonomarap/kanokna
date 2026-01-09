package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.domain.model.PromoCode;

/**
 * Inbound port for creating promo codes (admin operation).
 */
public interface CreatePromoCodeUseCase {
    PromoCode createPromoCode(String code, String description, String discountType,
                             double discountValue, Integer usageLimit,
                             String startDate, String endDate, String createdBy);
}

