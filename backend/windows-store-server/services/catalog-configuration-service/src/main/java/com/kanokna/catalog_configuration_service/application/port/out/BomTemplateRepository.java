package com.kanokna.catalog_configuration_service.application.port.out;

import com.kanokna.catalog_configuration_service.domain.model.BomTemplate;
import com.kanokna.shared.core.Id;

import java.util.List;

public interface BomTemplateRepository {

    List<BomTemplate> findByTemplate(Id templateId, Id tenantId);
}
