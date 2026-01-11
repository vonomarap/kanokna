package com.kanokna.pricing_service.adapters.out.persistence;

import org.springframework.stereotype.Component;
import com.kanokna.pricing_service.application.port.out.PromoCodeRepository;
import com.kanokna.pricing_service.domain.model.*;

import java.util.Optional;

/**
 * JPA adapter for promo code persistence.
 */
@Component
public class PromoCodeRepositoryAdapter implements PromoCodeRepository {
    private static final String DEFAULT_CURRENCY = "RUB";

    private final PromoCodeJpaRepository repository;

    public PromoCodeRepositoryAdapter(PromoCodeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PromoCode> findByCode(String code) {
        return repository.findByCodeIgnoreCase(code)
            .map(this::toDomain);
    }

    @Override
    public PromoCode save(PromoCode promoCode) {
        return toDomain(repository.save(toEntity(promoCode)));
    }

    private PromoCode toDomain(PromoCodeJpaEntity entity) {
        Money maxDiscount = entity.getMaxDiscount() != null
            ? Money.of(entity.getMaxDiscount(), DEFAULT_CURRENCY)
            : null;
        Money minSubtotal = entity.getMinSubtotal() != null
            ? Money.of(entity.getMinSubtotal(), DEFAULT_CURRENCY)
            : null;

        return PromoCode.restore(
            PromoCodeId.of(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getDiscountType(),
            entity.getDiscountValue(),
            maxDiscount,
            minSubtotal,
            entity.getUsageLimit(),
            entity.getUsageCount(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getCreatedBy()
        );
    }

    private PromoCodeJpaEntity toEntity(PromoCode promoCode) {
        PromoCodeJpaEntity entity = new PromoCodeJpaEntity();
        entity.setId(promoCode.getId().getValue());
        entity.setCode(promoCode.getCode());
        entity.setDescription(promoCode.getDescription());
        entity.setDiscountType(promoCode.getDiscountType());
        entity.setDiscountValue(promoCode.getDiscountValue());
        entity.setMaxDiscount(promoCode.getMaxDiscount() != null
            ? promoCode.getMaxDiscount().getAmount()
            : null);
        entity.setMinSubtotal(promoCode.getMinSubtotal() != null
            ? promoCode.getMinSubtotal().getAmount()
            : null);
        entity.setUsageLimit(promoCode.getUsageLimit());
        entity.setUsageCount(promoCode.getUsageCount());
        entity.setStartDate(promoCode.getStartDate());
        entity.setEndDate(promoCode.getEndDate());
        entity.setActive(promoCode.isActive());
        entity.setCreatedAt(promoCode.getCreatedAt());
        entity.setCreatedBy(promoCode.getCreatedBy());
        return entity;
    }
}
