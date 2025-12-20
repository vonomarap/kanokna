package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxRuleSpringRepository extends JpaRepository<TaxRuleJpaEntity, String> {
    Optional<TaxRuleJpaEntity> findFirstByRegion(String region);
}
