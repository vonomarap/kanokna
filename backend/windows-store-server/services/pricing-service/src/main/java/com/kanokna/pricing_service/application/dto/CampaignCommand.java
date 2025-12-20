package com.kanokna.pricing_service.application.dto;

import com.kanokna.pricing_service.domain.model.CampaignStatus;
import com.kanokna.shared.core.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record CampaignCommand(
    Id id,
    String name,
    CampaignStatus status,
    BigDecimal percentOff,
    Instant startsAt,
    Instant endsAt
) {
    public CampaignCommand {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(percentOff, "percentOff");
    }
}
