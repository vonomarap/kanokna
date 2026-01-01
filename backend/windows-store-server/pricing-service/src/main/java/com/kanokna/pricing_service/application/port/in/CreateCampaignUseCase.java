package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.CreateCampaignCommand;
import com.kanokna.pricing_service.domain.model.CampaignId;

/**
 * Inbound port for creating campaigns (admin operation).
 */
public interface CreateCampaignUseCase {
    CampaignId createCampaign(CreateCampaignCommand command);
}

