package com.kanokna.pricing_service.adapters.in.web.dto;

public record DecisionTraceDto(
    String blockId,
    String state,
    String detail
) { }
