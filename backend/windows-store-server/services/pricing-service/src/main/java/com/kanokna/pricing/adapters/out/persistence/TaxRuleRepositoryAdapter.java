package com.kanokna.pricing.adapters.out.persistence;

import org.springframework.stereotype.Component;
import com.kanokna.pricing.application.port.out.TaxRuleRepository;
import com.kanokna.pricing.domain.model.TaxRule;
import com.kanokna.pricing.domain.model.TaxRuleId;

import java.util.Optional;

/**
 * JPA adapter for tax rule persistence.
 */
@Component
public class TaxRuleRepositoryAdapter implements TaxRuleRepository {
    private final TaxRuleJpaRepository repository;

    public TaxRuleRepositoryAdapter(TaxRuleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TaxRule> findByRegion(String region) {
        return repository.findByRegionIgnoreCase(region)
            .map(this::toDomain);
    }

    @Override
    public TaxRule save(TaxRule taxRule) {
        return toDomain(repository.save(toEntity(taxRule)));
    }

    private TaxRule toDomain(TaxRuleJpaEntity entity) {
        return TaxRule.restore(
            TaxRuleId.of(entity.getId()),
            entity.getRegion(),
            entity.getRegionName(),
            entity.getTaxRatePercent(),
            entity.getTaxType(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }

    private TaxRuleJpaEntity toEntity(TaxRule taxRule) {
        TaxRuleJpaEntity entity = new TaxRuleJpaEntity();
        entity.setId(taxRule.getId().getValue());
        entity.setRegion(taxRule.getRegion());
        entity.setRegionName(taxRule.getRegionName());
        entity.setTaxRatePercent(taxRule.getTaxRatePercent());
        entity.setTaxType(taxRule.getTaxType());
        entity.setActive(taxRule.isActive());
        entity.setCreatedAt(taxRule.getCreatedAt());
        return entity;
    }
}
