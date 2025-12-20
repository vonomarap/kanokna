package com.kanokna.catalog_configuration_service.application.port.in;

import com.kanokna.catalog_configuration_service.application.dto.TemplateDraftCommand;

public interface TemplateCommandPort {

    void defineTemplate(TemplateDraftCommand command);

    void updateTemplateOptions(TemplateDraftCommand command);

    void defineRules(String templateId, String tenantId, TemplateDraftCommand command);

    void publishCatalogVersion(String templateId, String tenantId);
}
