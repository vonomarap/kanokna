package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

public record DecisionTraceDto(
    String blockId,
    String state,
    String detail
) { }
