package com.kanokna.catalog_configuration_service.domain.model;

import java.util.Objects;

/**
 * Captures belief-state markers emitted by domain services for adapters to log.
 */
public record DecisionTrace(
    String blockId,
    String state,
    String detail
) {
    public DecisionTrace {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(state, "state");
        detail = detail == null ? "" : detail;
    }
}
