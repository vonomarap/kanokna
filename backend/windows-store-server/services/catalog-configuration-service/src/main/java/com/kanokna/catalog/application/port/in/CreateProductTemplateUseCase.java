package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.CreateProductTemplateCommand;
import com.kanokna.catalog.domain.model.ProductTemplateId;

/**
 * Inbound port: Create a new product template (admin operation).
 */
public interface CreateProductTemplateUseCase {

    ProductTemplateId create(CreateProductTemplateCommand command);
}
