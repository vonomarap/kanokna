package com.kanokna.pricing_service.application.dto;

import com.kanokna.pricing_service.domain.model.Quote;
import com.kanokna.pricing_service.domain.service.DecisionTrace;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.List;

public record QuoteResponse(
    Id quoteId,
    Id priceBookId,
    long priceBookVersion,
    long catalogVersion,
    String region,
    Money base,
    Money optionsTotal,
    Money discountTotal,
    Money taxTotal,
    Money total,
    List<Id> appliedCampaigns,
    List<DecisionTrace> traces
) {
    public static QuoteResponse from(Quote quote, List<DecisionTrace> traces) {
        return new QuoteResponse(
            quote.id(),
            quote.priceBookId(),
            quote.priceBookVersion(),
            quote.catalogVersion(),
            quote.region(),
            quote.base(),
            quote.optionsTotal(),
            quote.discountTotal(),
            quote.taxTotal(),
            quote.total(),
            quote.appliedCampaigns(),
            traces
        );
    }
}
