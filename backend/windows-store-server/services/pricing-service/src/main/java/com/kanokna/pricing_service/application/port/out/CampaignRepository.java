package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.Campaign;
import java.util.List;

/**
 * Outbound port for campaign persistence.
 */
public interface CampaignRepository {
    List<Campaign> findActiveForProduct(String productTemplateId);

    Campaign save(Campaign campaign);
}
