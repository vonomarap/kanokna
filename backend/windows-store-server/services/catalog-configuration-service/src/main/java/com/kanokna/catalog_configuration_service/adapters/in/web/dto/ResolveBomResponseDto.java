package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

import java.util.List;

public record ResolveBomResponseDto(
    String templateId,
    String bomTemplateCode,
    List<BomItemDto> items
) { }
