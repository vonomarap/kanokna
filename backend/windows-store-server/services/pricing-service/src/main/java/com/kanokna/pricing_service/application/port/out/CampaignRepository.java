package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.Campaign;

import java.util.List;

public interface CampaignRepository {

    List<Campaign> findActiveForRegionAndSegment(String region, String customerSegment);
}
