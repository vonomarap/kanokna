package com.kanokna.pricing_service.domain.model;

import com.kanokna.pricing_service.domain.event.QuoteCalculatedEvent;
import com.kanokna.pricing_service.domain.service.DecisionTrace;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class QuoteCalculationResult {
    private final Quote quote;
    private final List<DecisionTrace> traces;
    private final QuoteCalculatedEvent event;

    public QuoteCalculationResult(Quote quote, List<DecisionTrace> traces, QuoteCalculatedEvent event) {
        this.quote = quote;
        this.traces = Collections.unmodifiableList(List.copyOf(traces));
        this.event = event;
    }

    public Quote quote() {
        return quote;
    }

    public List<DecisionTrace> traces() {
        return traces;
    }

    public Optional<QuoteCalculatedEvent> event() {
        return Optional.ofNullable(event);
    }
}
