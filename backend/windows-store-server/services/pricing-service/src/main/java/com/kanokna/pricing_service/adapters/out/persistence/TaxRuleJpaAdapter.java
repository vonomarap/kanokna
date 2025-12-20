package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.adapters.out.persistence.jpa.TaxRuleJpaEntity;
import com.kanokna.pricing_service.adapters.out.persistence.jpa.TaxRuleSpringRepository;
import com.kanokna.pricing_service.application.port.out.TaxRuleRepository;
import com.kanokna.pricing_service.domain.model.TaxRule;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TaxRuleJpaAdapter implements TaxRuleRepository {

    private final TaxRuleSpringRepository repository;

    public TaxRuleJpaAdapter(TaxRuleSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TaxRule> findForRegion(String region) {
        return repository.findFirstByRegion(region)
            .map(this::toDomain);
    }

    public void save(TaxRule rule) {
        repository.save(new TaxRuleJpaEntity(
            Id.random().value(),
            rule.region(),
            rule.productType(),
            rule.rate(),
            rule.roundingPolicyCode()
        ));
    }

    private TaxRule toDomain(TaxRuleJpaEntity entity) {
        return new TaxRule(
            entity.getRegion(),
            entity.getProductType(),
            entity.getRate(),
            entity.getRoundingPolicy()
        );
    }
}
