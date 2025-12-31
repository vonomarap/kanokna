package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.domain.model.ProductTemplateId;

/**
 * Inbound port: Get product template details.
 */
public interface GetProductTemplateQuery {

    ProductTemplateDto getById(ProductTemplateId productTemplateId);
}
