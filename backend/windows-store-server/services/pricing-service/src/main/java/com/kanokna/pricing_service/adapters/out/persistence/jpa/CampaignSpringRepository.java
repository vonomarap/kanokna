package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignSpringRepository extends JpaRepository<CampaignJpaEntity, String> {
    List<CampaignJpaEntity> findByStatus(String status);
}
