package com.kanokna.catalog_configuration_service.application.port.out;

import com.kanokna.catalog_configuration_service.domain.model.ProductTemplate;

public interface SearchIndexPort {

    void indexTemplate(ProductTemplate template);
}
