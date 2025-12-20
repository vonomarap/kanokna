package com.kanokna.catalog_configuration_service.application.dto;

import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.shared.core.Id;

import java.util.List;

public record ResolveBomResponse(
    Id templateId,
    String bomTemplateCode,
    List<BomTemplate.BomItem> items
) { }
