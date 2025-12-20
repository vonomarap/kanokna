package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

public record ValidationMessageDto(
    String severity,
    String code,
    String message,
    String attributeCode
) { }
