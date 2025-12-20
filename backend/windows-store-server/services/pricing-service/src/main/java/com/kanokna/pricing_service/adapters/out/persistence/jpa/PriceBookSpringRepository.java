package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceBookSpringRepository extends JpaRepository<PriceBookJpaEntity, String> {
    Optional<PriceBookJpaEntity> findFirstByRegionIgnoreCaseAndCurrencyAndStatus(String region, String currency, String status);
}
