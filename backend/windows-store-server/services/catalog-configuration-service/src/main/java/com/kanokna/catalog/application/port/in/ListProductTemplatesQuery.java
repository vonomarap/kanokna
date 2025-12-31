package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.domain.model.ProductFamily;

import java.util.List;

/**
 * Inbound port: List product templates with optional filtering.
 */
public interface ListProductTemplatesQuery {

    List<ProductTemplateDto> list(ProductFamily productFamily, boolean activeOnly);
}
