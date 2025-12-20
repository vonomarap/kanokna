package com.kanokna.pricing_service.adapters.in.web.dto;

import java.util.List;

public record QuoteResponseDto(
    String quoteId,
    String priceBookId,
    long priceBookVersion,
    long catalogVersion,
    String region,
    MoneyDto base,
    MoneyDto optionsTotal,
    MoneyDto discountTotal,
    MoneyDto taxTotal,
    MoneyDto total,
    List<String> appliedCampaigns,
    List<DecisionTraceDto> traces
) { }
