package com.kanokna.pricing_service.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromoCodeJpaRepository extends JpaRepository<PromoCodeJpaEntity, UUID> {
    Optional<PromoCodeJpaEntity> findByCodeIgnoreCase(String code);
}
