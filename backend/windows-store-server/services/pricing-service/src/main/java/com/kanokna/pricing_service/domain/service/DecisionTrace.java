package com.kanokna.pricing_service.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record DecisionTrace(String blockId, String state, String detail) {
    public DecisionTrace {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(state, "state");
        detail = detail == null ? "" : detail;
    }

    public static final class TraceCollector {
        private final List<DecisionTrace> traces = new ArrayList<>();

        public void trace(String blockId, String state, String detail) {
            traces.add(new DecisionTrace(blockId, state, detail));
        }

        public List<DecisionTrace> asImmutable() {
            return Collections.unmodifiableList(traces);
        }
    }
}
