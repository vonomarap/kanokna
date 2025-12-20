package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

import java.util.List;

public record ValidateConfigurationResponseDto(
    boolean valid,
    List<ValidationMessageDto> errors,
    List<ValidationMessageDto> warnings,
    List<DecisionTraceDto> traces
) { }
