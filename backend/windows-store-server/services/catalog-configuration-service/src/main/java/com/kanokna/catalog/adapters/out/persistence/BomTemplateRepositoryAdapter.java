package com.kanokna.catalog.adapters.out.persistence;

import com.kanokna.catalog.application.port.out.BomTemplateRepository;
import com.kanokna.catalog.domain.model.BomTemplate;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing BomTemplateRepository port.
 * Simplified stub implementation.
 */
@Component
public class BomTemplateRepositoryAdapter implements BomTemplateRepository {

    @Override
    public BomTemplate save(BomTemplate bomTemplate) {
        // Stub: would persist to bom_templates and bom_lines tables
        return bomTemplate;
    }

    @Override
    public Optional<BomTemplate> findById(UUID id) {
        // Stub: would query from database
        return Optional.empty();
    }

    @Override
    public Optional<BomTemplate> findByProductTemplateId(ProductTemplateId productTemplateId) {
        // Stub: would query BOM template for product
        return Optional.empty();
    }
}
