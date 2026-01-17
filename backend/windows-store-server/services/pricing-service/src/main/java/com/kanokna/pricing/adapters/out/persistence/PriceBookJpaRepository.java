package com.kanokna.pricing.adapters.out.persistence;

import com.kanokna.pricing.domain.model.PriceBookStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PriceBookJpaRepository extends JpaRepository<PriceBookJpaEntity, UUID> {
    @EntityGraph(attributePaths = "optionPremiums")
    Optional<PriceBookJpaEntity> findFirstByProductTemplateIdAndStatus(String productTemplateId, PriceBookStatus status);
}
