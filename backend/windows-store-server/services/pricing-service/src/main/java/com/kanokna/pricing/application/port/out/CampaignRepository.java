package com.kanokna.pricing.application.port.out;

import com.kanokna.pricing.domain.model.Campaign;
import java.util.List;

/**
 * Outbound port for campaign persistence.
 */
public interface CampaignRepository {
    List<Campaign> findActiveForProduct(String productTemplateId);

    Campaign save(Campaign campaign);
}
