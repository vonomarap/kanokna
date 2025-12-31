package com.kanokna.pricing.application.port.in;

import com.kanokna.pricing.application.dto.CreateCampaignCommand;
import com.kanokna.pricing.domain.model.CampaignId;

/**
 * Inbound port for creating campaigns (admin operation).
 */
public interface CreateCampaignUseCase {
    CampaignId createCampaign(CreateCampaignCommand command);
}
