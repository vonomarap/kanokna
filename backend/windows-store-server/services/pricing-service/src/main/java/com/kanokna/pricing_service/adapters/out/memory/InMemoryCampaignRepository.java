package com.kanokna.pricing_service.adapters.out.memory;

import com.kanokna.pricing_service.application.port.out.CampaignRepository;
import com.kanokna.pricing_service.domain.model.Campaign;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryCampaignRepository implements CampaignRepository {

    private final List<Campaign> campaigns = new CopyOnWriteArrayList<>();

    @Override
    public List<Campaign> findActiveForRegionAndSegment(String region, String customerSegment) {
        // For now return all; a real impl would filter by region/segment.
        return new ArrayList<>(campaigns);
    }

    public void save(Campaign campaign) {
        campaigns.add(campaign);
    }
}
