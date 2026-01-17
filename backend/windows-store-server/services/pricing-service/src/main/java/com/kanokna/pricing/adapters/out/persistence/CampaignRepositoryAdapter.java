package com.kanokna.pricing.adapters.out.persistence;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing.application.port.out.CampaignRepository;
import com.kanokna.pricing.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA adapter for campaign persistence.
 */
@Component
public class CampaignRepositoryAdapter implements CampaignRepository {
    private static final String DEFAULT_CURRENCY = "RUB";

    private final CampaignJpaRepository repository;
    private final ObjectMapper objectMapper;

    public CampaignRepositoryAdapter(CampaignJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Campaign> findActiveForProduct(String productTemplateId) {
        Instant now = Instant.now();
        return repository.findByStatusAndStartDateBeforeAndEndDateAfter(CampaignStatus.ACTIVE, now, now)
            .stream()
            .map(this::toDomain)
            .filter(campaign -> campaign.isApplicableTo(productTemplateId))
            .collect(Collectors.toList());
    }

    @Override
    public Campaign save(Campaign campaign) {
        CampaignJpaEntity entity = toEntity(campaign);
        return toDomain(repository.save(entity));
    }

    private Campaign toDomain(CampaignJpaEntity entity) {
        Money maxDiscount = entity.getMaxDiscount() != null
            ? Money.of(entity.getMaxDiscount(), DEFAULT_CURRENCY)
            : null;
        CampaignRule rule;
        if (entity.getDiscountType() == DiscountType.PERCENTAGE) {
            rule = CampaignRule.percentage(entity.getDiscountValue(), maxDiscount);
        } else {
            rule = CampaignRule.fixed(Money.of(entity.getDiscountValue(), DEFAULT_CURRENCY));
        }

        return Campaign.restore(
            CampaignId.of(entity.getId()),
            entity.getName(),
            entity.getDescription(),
            rule,
            parseApplicableProducts(entity.getApplicableProducts()),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus(),
            entity.getPriority(),
            entity.getCreatedAt(),
            entity.getCreatedBy()
        );
    }

    private CampaignJpaEntity toEntity(Campaign campaign) {
        CampaignJpaEntity entity = new CampaignJpaEntity();
        entity.setId(campaign.getId().getValue());
        entity.setName(campaign.getName());
        entity.setDescription(campaign.getDescription());
        entity.setDiscountType(campaign.getRule().getDiscountType());
        entity.setDiscountValue(campaign.getRule().getDiscountValue());
        entity.setMaxDiscount(campaign.getRule().getMaxDiscount() != null
            ? campaign.getRule().getMaxDiscount().getAmount()
            : null);
        entity.setApplicableProducts(writeApplicableProducts(campaign.getApplicableProducts()));
        entity.setStartDate(campaign.getStartDate());
        entity.setEndDate(campaign.getEndDate());
        entity.setStatus(campaign.getStatus());
        entity.setPriority(campaign.getPriority());
        entity.setCreatedAt(campaign.getCreatedAt());
        entity.setCreatedBy(campaign.getCreatedBy());
        return entity;
    }

    private Set<String> parseApplicableProducts(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptySet();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Set<String>>() {});
        } catch (Exception ex) {
            return Collections.emptySet();
        }
    }

    private String writeApplicableProducts(Set<String> products) {
        if (products == null || products.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(products);
        } catch (Exception ex) {
            return "[]";
        }
    }
}
