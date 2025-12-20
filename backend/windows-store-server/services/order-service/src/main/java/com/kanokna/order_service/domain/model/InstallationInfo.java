package com.kanokna.order_service.domain.model;

import java.time.Instant;
import java.util.Objects;

public record InstallationInfo(
    Instant scheduledAt,
    String installerId,
    String notes
) {
    public InstallationInfo {
        Objects.requireNonNull(scheduledAt, "scheduledAt");
    }
}
