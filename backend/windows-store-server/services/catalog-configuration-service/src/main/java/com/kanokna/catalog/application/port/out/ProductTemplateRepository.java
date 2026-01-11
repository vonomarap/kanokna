package com.kanokna.catalog.application.port.out;

import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplate;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import com.kanokna.catalog.domain.model.TemplateStatus;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: Product template persistence.
 */
public interface ProductTemplateRepository {

    ProductTemplate save(ProductTemplate productTemplate);

    Optional<ProductTemplate> findById(ProductTemplateId id);

    List<ProductTemplate> findByStatus(TemplateStatus status);

    List<ProductTemplate> findByProductFamilyAndStatus(ProductFamily productFamily, TemplateStatus status);

    List<ProductTemplate> findAll();

    boolean existsByNameAndProductFamily(String name, ProductFamily productFamily);
}
