package com.kanokna.catalog_configuration_service.application.port.out;

import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Optional;

public interface ProductTemplateRepository {

    Optional<ProductTemplate> findActiveById(Id templateId, Id tenantId);

    List<ProductTemplate> findActiveByTenant(Id tenantId);

    void save(ProductTemplate template);
}
