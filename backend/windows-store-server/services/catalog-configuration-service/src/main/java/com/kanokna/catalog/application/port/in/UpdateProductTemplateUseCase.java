package com.kanokna.catalog.application.port.in;

import com.kanokna.catalog.application.dto.UpdateProductTemplateCommand;

/**
 * Inbound port: Update an existing product template (admin operation).
 */
public interface UpdateProductTemplateUseCase {

    void update(UpdateProductTemplateCommand command);
}
