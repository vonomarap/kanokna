package com.kanokna.pricing.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaxRuleJpaRepository extends JpaRepository<TaxRuleJpaEntity, UUID> {
    Optional<TaxRuleJpaEntity> findByRegionIgnoreCase(String region);
}
