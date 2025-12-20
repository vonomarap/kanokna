package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ResolveBomRequest(
    @NotBlank String templateId,
    @NotBlank String tenantId,
    @Min(1) int widthCm,
    @Min(1) int heightCm,
    Map<String, String> optionSelections,
    @NotBlank String locale,
    @Min(0) long catalogVersion
) { }
