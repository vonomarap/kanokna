package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.domain.model.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CampaignJpaRepository extends JpaRepository<CampaignJpaEntity, UUID> {
    List<CampaignJpaEntity> findByStatusAndStartDateBeforeAndEndDateAfter(CampaignStatus status, Instant now, Instant now2);
}
