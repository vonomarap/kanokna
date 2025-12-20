package com.kanokna.catalog_configuration_service.adapters.in.web.dto;

public record BomItemDto(
    String sku,
    int quantity,
    String unit
) { }
