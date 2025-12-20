package com.kanokna.order_service.application.dto;

import com.kanokna.shared.core.Id;

import java.time.Instant;
import java.util.Objects;

public record ScheduleInstallationCommand(
    Id orderId,
    Instant scheduledAt,
    String installerId,
    String notes
) {
    public ScheduleInstallationCommand {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(scheduledAt, "scheduledAt");
    }
}
