package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.adapters.out.persistence.jpa.CampaignJpaEntity;
import com.kanokna.pricing_service.adapters.out.persistence.jpa.CampaignSpringRepository;
import com.kanokna.pricing_service.application.port.out.CampaignRepository;
import com.kanokna.pricing_service.domain.model.Campaign;
import com.kanokna.pricing_service.domain.model.CampaignStatus;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CampaignJpaAdapter implements CampaignRepository {

    private final CampaignSpringRepository repository;

    public CampaignJpaAdapter(CampaignSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Campaign> findActiveForRegionAndSegment(String region, String customerSegment) {
        return repository.findByStatus(CampaignStatus.ACTIVE.name())
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public void save(Campaign campaign) {
        repository.save(toEntity(campaign));
    }

    private Campaign toDomain(CampaignJpaEntity entity) {
        return new Campaign(
            Id.of(entity.getId()),
            entity.getName(),
            CampaignStatus.valueOf(entity.getStatus()),
            entity.getPercentOff(),
            entity.getStartsAt(),
            entity.getEndsAt()
        );
    }

    private CampaignJpaEntity toEntity(Campaign campaign) {
        return new CampaignJpaEntity(
            campaign.id().value(),
            campaign.name(),
            campaign.status().name(),
            campaign.percentOff(),
            campaign.startsAt(),
            campaign.endsAt(),
            null
        );
    }
}
