package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.shared.measure.DimensionsCm;

import java.util.Optional;

public record DimensionPolicy(
    int minWidthCm,
    int maxWidthCm,
    int minHeightCm,
    int maxHeightCm,
    int stepMm
) {
    public DimensionPolicy {
        if (minWidthCm <= 0 || minHeightCm <= 0 || maxWidthCm <= 0 || maxHeightCm <= 0) {
            throw new IllegalArgumentException("Dimension bounds must be positive");
        }
        if (minWidthCm > maxWidthCm || minHeightCm > maxHeightCm) {
            throw new IllegalArgumentException("Minimum dimensions must be <= maximum dimensions");
        }
        if (stepMm <= 0) {
            throw new IllegalArgumentException("stepMm must be positive");
        }
    }

    public boolean withinRange(DimensionsCm dimensions) {
        return dimensions.width() >= minWidthCm &&
            dimensions.width() <= maxWidthCm &&
            dimensions.height() >= minHeightCm &&
            dimensions.height() <= maxHeightCm &&
            alignsToStep(dimensions.width()) &&
            alignsToStep(dimensions.height());
    }

    public Optional<String> violationMessage(DimensionsCm dimensions) {
        if (withinRange(dimensions)) {
            return Optional.empty();
        }
        String message = "Dimensions %d x %d cm violate policy [%d-%d cm width, %d-%d cm height, step=%dmm]"
            .formatted(dimensions.width(), dimensions.height(), minWidthCm, maxWidthCm, minHeightCm, maxHeightCm, stepMm);
        return Optional.of(message);
    }

    private boolean alignsToStep(int valueCm) {
        int stepCm = stepMm / 10;
        return stepCm <= 0 || valueCm % stepCm == 0;
    }
}
