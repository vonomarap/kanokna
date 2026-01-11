package com.kanokna.catalog.application.port.out;

import com.kanokna.catalog.domain.model.BomTemplate;
import com.kanokna.catalog.domain.model.ProductTemplateId;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: BOM template persistence.
 */
public interface BomTemplateRepository {

    BomTemplate save(BomTemplate bomTemplate);

    Optional<BomTemplate> findById(UUID id);

    Optional<BomTemplate> findByProductTemplateId(ProductTemplateId productTemplateId);
}
